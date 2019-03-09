# Pirates game

An open source game to demo the integration of several Crescent Flare libraries together.


## The development server

Inside the repository there is a nodejs-based development server which can be used for hot reloading changes in the page json files or sending events to the app (which is further explained below). After nodejs is installed, run the server and check the terminal to see on which IP-address it runs on.

### Enabling the development server in the app

The app contains a debug menu which can be opened by shaking the device on iOS, or by long-pressing the action bar (which works even if it's fully transparent) on Android. In the menu there is a field to enter the IP-address of the development server. Also it contains switches to enable/disable hot reloading and sending events.

### Hot reloading pages

The libraries and framework of the app allow it to load, parse and show every screen by defining it in a page json file. These files are delivered with the app, but can also be sent through the network. The page loader and related classes are able to hot reload these files by long polling the development server for changes. When a json file is changed, it loads the new file and refreshes the screen. Enable it in the debug menu.

### Sending and receiving events

The app defines events in a common structure which can be linked to component interactions. For example, a button component can send an event to the view controller or activity when it's tapped. They are defined as a json object, or as an URI string. The development server contains a tool to enter events in URI format (through the command console) to be sent to a client. A client can connect and receive events through the event receiver tool which is part of the app. Enable it in the debug menu.
