# fileobserver

# General information

An application that tracks changes to files. 

The user indicates the file on which he wants to track changes. 

When text is added to the file the user will receive an email notification
that the text has been added to the file and the content that has been added.

One user can observe multiple files and one file could be observed by multiple users

All tables in database are created through liquibase. 
Liquibase also create users with roles.
