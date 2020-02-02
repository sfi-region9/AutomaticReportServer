<h1 align="center">Automatic Report Server 👋</h1>
<p>
  <img alt="Version" src="https://img.shields.io/badge/version-2.0-blue.svg?cacheSeconds=2592000" />
  <a href="https://doc.sfiars.eu" target="_blank">
    <img alt="Documentation" src="https://img.shields.io/badge/documentation-yes-brightgreen.svg" />
  </a>
  <a href="#" target="_blank">
    <img alt="License: MIT" src="https://img.shields.io/badge/License-MIT-yellow.svg" />
  </a>
  <a href="https://twitter.com/coco33920" target="_blank">
    <img alt="Twitter: coco33920" src="https://img.shields.io/twitter/follow/coco33920.svg?style=social" />
  </a>
</p>

> The easiest way to report to your commanding officer

### 🏠 [Homepage](https://client.sfiars.eu)

### ✨ [Demo](https://api.sfiars.eu)

## Install

* Download source ```$git clone https://github.com/sfi-region9/AutomaticReportServer```
* Add the configuration page to src/main/resources `$touch config.json`
* Copy the following configuration structure :
```json5 
    {
      "accessToken":"facebookPageToken",
      "adminID":"adminMessengerID",
      "secretKey":"facebookAppSecretKey",
      "verifyToken":"facebookWebhookToken",
      "dbHost":"host",
      "dbName":"name",
      "dbUser":"user",
      "dbPassword":"password",
      "dbUserName":"userDatabase"
    }
```
* Build the server with `$mvn clean install`
* Run the jar-with-dependencies
* See the client here [Client](https://github.com/sfi-region9/ARS-Client-Web)

## Author

👤 **Charlotte THOMAS**

* Website: https://client.sfiars.eu
* Twitter: [@coco33920](https://twitter.com/coco33920)
* Github: [@coco33920](https://github.com/coco33920)

## Show your support

Give a ⭐️ if this project helped you!

***
_This README was generated with ❤️ by [readme-md-generator](https://github.com/kefranabg/readme-md-generator)_
