# AWS::EntityResolution::IdMappingWorkflow IdMappingWorkflowOutputSource

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#outputs3path" title="OutputS3Path">OutputS3Path</a>" : <i>String</i>,
    "<a href="#kmsarn" title="KMSArn">KMSArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#outputs3path" title="OutputS3Path">OutputS3Path</a>: <i>String</i>
<a href="#kmsarn" title="KMSArn">KMSArn</a>: <i>String</i>
</pre>

## Properties

#### OutputS3Path

The S3 path to which Entity Resolution will write the output table

_Required_: Yes

_Type_: String

_Pattern_: <code>^s3://([^/]+)/?(.*?([^/]+)/?)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KMSArn

_Required_: No

_Type_: String

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn):kms:.*:[0-9]+:.*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
