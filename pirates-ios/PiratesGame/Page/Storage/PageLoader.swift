//
//  PageLoader.swift
//  Page storage: handles loading of pages from several sources
//

import Foundation
import Alamofire

protocol PageLoaderContinuousCompletion: class {
    
    func didUpdatePage(page: Page)
    
}

class PageLoader {
    
    // --
    // MARK: Members
    // --

    private let location: String
    private let loadInternal: Bool
    private weak var continuousCompletion: PageLoaderContinuousCompletion?
    private var loading = false
    private var waiting = false
    

    // --
    // MARK: Initialization
    // --
    
    init(location: String) {
        self.location = location
        loadInternal = !location.contains("://")
    }
    

    // --
    // MARK: Loading
    // --
    
    func load(completion: @escaping (_ page: Page?, _ error: Error?) -> Void) {
        if let page = PageCache.shared.getEntry(cacheKey: location) {
            completion(page, nil)
        } else if loadInternal {
            loadInternal(completion: { page, error in
                if let page = page {
                    PageCache.shared.storeEntry(cacheKey: self.location, page: page)
                }
                completion(page, error)
            })
        } else {
            loadOnline(currentHash: "ignore", completion: { page, error in
                if let page = page {
                    PageCache.shared.storeEntry(cacheKey: self.location, page: page)
                }
                completion(page, error)
            })
        }
    }
    
    private func loadOnline(currentHash: String, completion: @escaping (_ page: Page?, _ error: Error?) -> Void) {
        if !loadInternal {
            let headers: HTTPHeaders = [
                "X-Mock-Wait-Change-Hash": currentHash
            ]
            loading = true
            Alamofire.request(location, headers: headers).responseJSON { response in
                self.loading = false
                if let dictionary = response.value as? [String: Any] {
                    completion(Page(dictionary: dictionary, hash: response.data?.md5() ?? "unknown"), nil)
                } else {
                    completion(nil, response.error)
                }
            }
        } else {
            completion(nil, NSError(domain: "Loading mismatch", code: -1))
        }
    }
    
    private func loadInternal(completion: @escaping (_ page: Page?, _ error: Error?) -> Void) {
        loading = true
        DispatchQueue.global().async {
            let page = self.loadInternalSync()
            DispatchQueue.main.async {
                self.loading = false
                completion(page, page == nil ? NSError(domain: "Could not load page", code: -1) : nil)
            }
        }
    }
    
    func loadInternalSync() -> Page? {
        if loadInternal {
            if let path = Bundle.main.path(forResource: "Pages/" + location, ofType: "json") {
                if let jsonData = try? NSData(contentsOfFile: path, options: .mappedIfSafe) as Data {
                    return Page(jsonData: jsonData)
                }
            }
        }
        return nil
    }

   
    // --
    // MARK: Continuous loading
    // --
    
    func startLoadingContinuously(completion: PageLoaderContinuousCompletion) {
        continuousCompletion = completion
        tryNextContinuousLoad()
    }
    
    func stopLoadingContinuously() {
        continuousCompletion = nil
    }
    
    private func tryNextContinuousLoad() {
        if !loading && !waiting && continuousCompletion != nil {
            if loadInternal {
                loadInternal(completion: { page, error in
                    if let page = page {
                        PageCache.shared.storeEntry(cacheKey: self.location, page: page)
                        self.continuousCompletion?.didUpdatePage(page: page)
                    }
                })
            } else {
                let hash = PageCache.shared.getEntry(cacheKey: location)?.hash ?? "unknown"
                loadOnline(currentHash: hash, completion: { page, error in
                    var waitingTime: Double = 2
                    if let page = page {
                        if hash != page.hash {
                            PageCache.shared.storeEntry(cacheKey: self.location, page: page)
                            self.continuousCompletion?.didUpdatePage(page: page)
                        }
                        waitingTime = 0.1
                    }
                    self.waiting = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + waitingTime, execute: {
                        self.waiting = false
                        self.tryNextContinuousLoad()
                    })
                })
            }
        }
    }

}
