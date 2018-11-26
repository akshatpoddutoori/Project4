# Project4
v2.0 Features:
- checks if a client username is unique
	- allows client to connect but disconnects if username is taken
	- poor solution? probably shouldn’t allow to connect at all
- broadcast works as intended
- /list
	- lists usernames of all connected clients except yourself
- /msg recipient message
	- can’t direct message yourself
	- allows sending empty message
- /logout
	- works as expected but uses System.exit(0)
	- force quitting disconnects without LOGOUT message
- ChatFilter
	- case insensitive to the word list
	- runs server with no banned words if file not found

Known issues:
- doesn’t prompt with ‘> ’
