#!/usr/bin/bash

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

printf "${GREEN}==> ${NC}Setting up Appwrite Project for classroom o.o${NC}\n"

printf "${GREEN}==> ${NC}Creating a new project with the id classroom and name Classroom...${NC}\n"

printf "\n${GREEN}==> ${NC}Enter the organization where you want to create the new project: ${NC}"
read organizationID

# Create project
appwrite projects create --projectId classroom --name Classroom --teamId $organizationID >> /dev/null

# Create database
printf "\n${GREEN}==> ${NC}Creating database for storing data...."
appwrite databases create --databaseId classes --name Classes >> /dev/null
printf "\n${GREEN}==> ${NC}Successfully created the database"

# Create registry collection
printf "\n\n${GREEN}==> ${NC}Setting up Registry collection..."
appwrite databases createCollection --databaseId classes --documentSecurity true --collectionId registry --name Registry >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId registry --key name --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId registry --key subject --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId registry --key color --size 50 --required true >> /dev/null

printf "\n${GREEN}==> ${NC}Successfully completed setting up Registry collection!"

# Create assignment collection
printf "\n\n${GREEN}==> ${NC}Setting up Assignment collection..."
appwrite databases createCollection --databaseId classes --documentSecurity true --collectionId assignments --name Assignments >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key title --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key author --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key description --size 1000 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key classid --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key authorId --size 50 --required true >> /dev/null
appwrite databases createIntegerAttribute --databaseId classes --collectionId assignments --key grade --required true >> /dev/null
appwrite databases createDatetimeAttribute --databaseId classes --collectionId assignments --key due_date --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId assignments --key attachments --required false --size 50 --array true >> /dev/null 
printf "\n${GREEN}==> ${NC}Successfully completed setting up Assignment collection!"

printf "\n\n${GREEN}==> ${NC}Setting up Announcement collection..."
appwrite databases createCollection --databaseId classes --documentSecurity true --collectionId announcements --name Announcements >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId announcements --key author --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId announcements --key message --size 1000 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId announcements --key attachments --size 50 --required false --array true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId announcements --key classid --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId announcements --key userId --size 50 --required true >> /dev/null
printf "\n${GREEN}==> ${NC}Successfully completed setting up Announcement collection!"

printf "\n\n${GREEN}==> ${NC}Setting up Submissions collection..."
appwrite databases createCollection --databaseId classes --documentSecurity true --collectionId submissions --name Submissions >> /dev/null
appwrite databases createIntegerAttribute --databaseId classes --collectionId submissions --key grade --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId submissions --key comments --size 100 --required false --array true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId submissions --key submissions --size 10 --required false --array true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId submissions --key studentId --size 50 --required true >> /dev/null
appwrite databases createStringAttribute --databaseId classes --collectionId submissions --key studentName --size 50 --required true >> /dev/null
printf "\n${GREEN}==> ${NC}Successfully completed setting up Submissions collection!"

printf "\n\n${GREEN}==> ${NC}Setting up buckets..."
appwrite storage createBucket --bucketId attachments --name Attachments --fileSecurity true >> /dev/null
appwrite storage createBucket --bucketId userpfps --name "User pfp" --fileSecurity true >> /dev/null
appwrite storage createBucket --bucketId submissions --name Submissions --fileSecurity true >> /dev/null

printf "\n\n${GREEN}==> ${NC}Successfully completed spinning up the Appwrite instance!"
printf "\n${GREEN}==> ${BLUE}Next step, create a new Google OAuth Credentials and add it to Appwrite Auth to add support for Google OAuth!\n"
