# Desk Labels Slack Lambda

An AWS Lambda to notify Slack when new labels are created in Desk.com, written in Scala

# Running locally

- Ensure you have ``mobile`` AWS credentials
- Setup your local config file:
```
cat <<EOF > /.gu/desk-labels-slack-lambda.conf
desk {
url="<desk url>"
email="<email account>"
pass="<password>"
}

s3 {
bucket="<s3 bucket to store existing labels>"
key="<csv file in s3 that stores the labels>"
}

slack {
url="<ulr for Slack integration>"
}
EOF
```
- Populate all the required fields in the config file
- ``sbt run``