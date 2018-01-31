# AndroidHttpServerDemo

AndroidHttpServerDemo is an app to demonstrate that Android device can be an HTTP server.

# How to run

Note that the HTTP server in the Android device can be accessed via local network, not via internet. Therefore, the HTTP server can only be accessed by devices which is connected to the same WiFi with the Android device running HTTP server.

## Install app

* clone this repository

```
$ git clone https://github.com/chooyan-eng/AndroidHttpServerDemo
```

* open the project with AndroidStudio
* simply run the project, and HTTP server will run right after running the app

## Connect to the HTTP server

* Check the local IP address of Android device. The local IP address will be found with operation below.
  1. open "Settings" app 
  1. select "Wi-Fi"
  1. select WiFi you are connecting
* open the browser on your PC (or a device with a web browser)
* open the URL below
  * `http://xxx.xxx.xxx.xxx/index.html`
  * `xxx.xxx.xxx.xxx` needs to be replaced with the local IP address you found at the previous operation.

Then, you will get the page like this.

![Image of index.html](https://github.com/chooyan-eng/code-your-ruby/blob/image/image/image_local_basic.png)

# Contributing

Bug reports or pull requests to make the app better are welcome. This project is intended to be a safe, welcoming space for collaboration, and contributors are expected to adhere to the Contributor Covenant code of conduct. 

# License

The project is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
