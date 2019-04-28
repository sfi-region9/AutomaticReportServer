<h1>ARS Reloaded</h1>
<h2>Server</h2>
<p>The Automatic Report Server is the new and easiest way to setup easily a report system for your chapter.</p>
<br>
<h3>How to Register<h3>
<p>It's very simple, talk about that to your CO and if he/she interested talk to the Automatic Report Server Bot at https://m.me/arsserverreloaded and stat a new conversation. <br> the command !help or any word trigger the help method and you can see all the commands, the most important is !subscribe, this command allow your CO to register the chapter, after a quick review the administrator accept or not the register ! And that is it ! <br> You can now use the clients like https://github.com/coco33920/AutomaticReportClient or any client you build :) ( API use in wiki ! )</p>
  
<br><br>
<h3>How it's work ?</h3>
<p>It's an HTTP Api connected to the SEND Api of facebook with the library messenger4j, it has 4 endpoints <br>
the / hook the facebook webhook
the /register_user register a new user ( not vessels ) who want to use the system
the /submit update the user report in the database
the /send trigger a special sending in case of problems ( a password system will be setup )</p>

<br><br>
<h3>Build it yourself</h3>
<p>Just setup your key with https://developers.facebook.com and give your keys. after download the source and maven :
  <strong>mvn clean install</strong> and use the file "ARS*****-with-dependencies.jar" put this jar on a server ( like a little VPS ) and run it ! ( build also your client )</p>
  <br><br>
  <h3>Contribute</h3>
  <p>All issues, pull request or stars are warm welcomed thanks :D !</p>
  <p>Developped and Maintain by LT Colin THOMAS from the USS Versailles, R9</p>
  <p>Hosted by USS Versailles</p>
<p>Thanks to https://github.com/ananagame for the Database class</p>
<p>http://sfi.org</p>
