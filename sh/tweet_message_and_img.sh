#!/bin/bash

toolhome=
uploaddir=

classpath=

tokenFileFullPath=
tweetMessageTextFullPath=
upImageDirectoryFullPath=
consumerKey=
consumerSecret=
twitpicAPIKey=
weekNo=
dataBeseServerURL=
dataBeseUserName=
dataBeseUserPass=

java -classpath ${classpath} jp.co.rough_and_ready.twitter.MessageWriteToTwitter ${tokenFileFullPath} ${tweetMessageTextFullPath} ${upImageDirectoryFullPath} ${consumerKey} ${consumerSecret} ${twitpicAPIKey} ${weekNo} ${dataBeseServerURL} ${dataBeseUserName} ${dataBeseUserPass}
