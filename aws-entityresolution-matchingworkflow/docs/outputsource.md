# AWS::EntityResolution::MatchingWorkflow OutputSource

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#outputs3path" title="OutputS3Path">OutputS3Path</a>" : <i>String</i>,
    "<a href="#output" title="Output">Output</a>" : <i>[ <a href="outputattribute.md">OutputAttribute</a>, ... ]</i>,
    "<a href="#kmsarn" title="KMSArn">KMSArn</a>" : <i>String</i>,
    "<a href="#applynormalization" title="ApplyNormalization">ApplyNormalization</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#outputs3path" title="OutputS3Path">OutputS3Path</a>: <i>String</i>
<a href="#output" title="Output">Output</a>: <i>
      - <a href="outputattribute.md">OutputAttribute</a></i>
<a href="#kmsarn" title="KMSArn">KMSArn</a>: <i>String</i>
<a href="#applynormalization" title="ApplyNormalization">ApplyNormalization</a>: <i>Boolean</i>
</pre>

## Properties

#### OutputS3Path

The S3 path to which Entity Resolution will write the output table

_Required_: Yes

_Type_: String

_Pattern_: <code>^s3://([^/]+)/?(.*?([^/]+)/?)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Output

_Required_: Yes

_Type_: List of <a href="outputattribute.md">OutputAttribute</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KMSArn

_Required_: No

_Type_: String

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn):kms:.*:[0-9]+:.*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApplyNormalization

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
