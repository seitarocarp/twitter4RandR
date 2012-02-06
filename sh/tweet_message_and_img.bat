

set classpath=

set tokenFileFullPath=
set tweetMessageTextFullPath=
set upImageDirectoryFullPath=
set consumerKey=
set consumerSecret=
set twitpicAPIKey=
set weekNo=1
set dataBeseServerURL=
set dataBeseUserName=
set dataBeseUserPass=



java -classpath %classpath% jp.co.rough_and_ready.twitter.MessageWriteToTwitter %tokenFileFullPath% %tweetMessageTextFullPath% %upImageDirectoryFullPath% %consumerKey% %consumerSecret% %twitpicAPIKey% %weekNo% %dataBeseServerURL% %dataBeseUserName% %dataBeseUserPass%