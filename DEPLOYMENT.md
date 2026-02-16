# 🚀 LinkyLink Deployment Guide

## Step 1: Prerequisites (your Mac)

```bash
# Install Java 17 (if not already)
brew install openjdk@17

# Install Node.js (if not already)
brew install node

# Install Maven (needed to generate the wrapper)
brew install maven

# Install AWS CLI
brew install awscli

# Configure AWS credentials (you'll need an AWS account)
aws configure
# → Enter your Access Key ID, Secret Access Key, region (us-east-1)
```

## Step 2: Create DynamoDB Tables (AWS Console)

The app auto-creates tables, but you can also do it manually:

1. Go to **AWS Console → DynamoDB**
2. Create table **`LinkyLinkUsers`** with partition key `username` (String)
3. Create table **`LinkyLinks`** with partition key `keyword` (String)
4. Use **On-demand** billing mode for both

## Step 3: Generate the Maven Wrapper

The `build.sh` script uses `./mvnw` which needs to be generated once:

```bash
cd /Users/rpurigel/personal/linkylink
mvn wrapper:wrapper -Dmaven=3.9.6
```

This creates `mvnw`, `mvnw.cmd`, and the `.mvn/` directory.

## Step 4: Test Locally

```bash
cd /Users/rpurigel/personal/linkylink

# Terminal 1: Start the backend
export JWT_SECRET="my-super-secret-key-at-least-32-chars-long!!"
mvn spring-boot:run

# Terminal 2: Start the React dev server
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173/app/** → Register → Create links!

## Step 5: Build the Single JAR

```bash
cd /Users/rpurigel/personal/linkylink
chmod +x build.sh
./build.sh
```

This builds React into Spring Boot's static resources, then packages everything into a single JAR at **`target/linkylink-1.0.0.jar`**.

## Step 6: Launch EC2 Instance

1. Go to **AWS Console → EC2 → Launch Instance**
2. **Name**: `linkylink-server`
3. **AMI**: Amazon Linux 2023
4. **Instance type**: `t2.micro` (Free Tier eligible)
5. **Key pair**: Create new → download the `.pem` file
6. **Security Group**: Allow SSH (22) + HTTP (80) from your IP
7. **IAM Role**: Create one with `AmazonDynamoDBFullAccess` policy and attach it
8. Launch!

## Step 7: Deploy to EC2

```bash
# Copy the JAR to EC2
scp -i ~/mykey.pem target/linkylink-1.0.0.jar ec2-user@<EC2-IP>:~/

# SSH into EC2
ssh -i ~/mykey.pem ec2-user@<EC2-IP>

# On EC2: Install Java
sudo yum install -y java-17-amazon-corretto

# Run the app (port 80 requires sudo)
export JWT_SECRET="pick-a-strong-secret-at-least-32-characters!!"
sudo -E java -jar linkylink-1.0.0.jar --server.port=80
```

## Step 8: Set Up `go` Shortcut

### Option A: Browser search engine (recommended)

Chrome → Settings → Search Engines → Add:
- **Name**: LinkyLink
- **Shortcut**: go
- **URL**: `http://<EC2-IP>/%s`

Now type **`go google`** in the address bar!

### Option B: /etc/hosts (for `go/google` with a slash)

```bash
sudo sh -c 'echo "<EC2-IP>  go" >> /etc/hosts'
```

Now **`go/google`** works directly in the browser!

---

## Architecture Overview

- **Backend**: Spring Boot 3.2.5 (Java 17) with Spring Security + JWT auth
- **Frontend**: React 18 + Vite, served from `/app/`
- **Database**: AWS DynamoDB (tables: `LinkyLinkUsers`, `LinkyLinks`)
- **Deployment**: Single executable JAR (React bundled inside)
- **Config file**: `src/main/resources/application.properties`
