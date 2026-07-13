**Ent Core Data Collections App (Backend)**

This repo contains the Ent Core Data Collection app backend. The Application is hosted on port 9010 on localhost with the api documentation on [localhost:9009/swagger-ui/index.html](localhost:9009/swagger-ui/index.html). 


---

## Setup Application locally

To begin using Ent Core Ledger  app backend in your local environment, kindly ensure you have access to our AWS account and have docker running on your machine.

1. Login the AWS image registry by running the following command in commandline `aws ecr-public  get-login-password --region us-east-1 | docker login --username AWS --password-stdin  public.ecr.aws/i4n1u1e7/`. (please add your profile to the command if you configured your aws cli with profile). 
A successful login should return `Login Succeeded`. 


Kindly ensure you have configured your **AWS CLI** for easy. To install **AWS CLI** kindly follow this [AWS guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) 

    a. For AWS Users  
    Create access keys by following the [aws guide](https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html). Configure your aws cli by following the [aws guide](https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-options.html) or [youtube video](https://www.youtube.com/watch?v=Rp-A84oh4G8&ab_channel=StephaneMaarek)

    b. For AWS Single Sign On Users
    You can follow this [aws guide](https://docs.aws.amazon.com/cli/latest/userguide/sso-configure-profile-token.html) or [youtube video](https://www.youtube.com/watch?v=YzNX_YZHPXk&ab_channel=RichDevelops)  to configure your **AWS CLI** when using an aws single sign on (SSO).


2. cd into the base of the project and run:

        1. Start database in detacted mode
            ```
            docker compose up -d postgresdb 
            ```

        2. Start project build
            ```
            docker compose build 
            ```

        3. Start project 
            ```
            docker compose up 
            ```
    

3. The project's backend should be running on [localhost:9009](localhost:9009), with the api documentation on [localhost:9009/swagger-ui/index.html](localhost:9009/swagger-ui/index.html)


---
