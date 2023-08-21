# AWS::EntityResolution::MatchingWorkflow InputSource

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#inputsourcearn" title="InputSourceARN">InputSourceARN</a>" : <i>String</i>,
    "<a href="#schemaarn" title="SchemaArn">SchemaArn</a>" : <i>String</i>,
    "<a href="#applynormalization" title="ApplyNormalization">ApplyNormalization</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#inputsourcearn" title="InputSourceARN">InputSourceARN</a>: <i>String</i>
<a href="#schemaarn" title="SchemaArn">SchemaArn</a>: <i>String</i>
<a href="#applynormalization" title="ApplyNormalization">ApplyNormalization</a>: <i>Boolean</i>
</pre>

## Properties

#### InputSourceARN

An Glue table ARN for the input source table

_Required_: Yes

_Type_: String

_Pattern_: <code>arn:(aws|aws-us-gov|aws-cn):.*:.*:[0-9]+:.*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SchemaArn

The SchemaMapping arn associated with the Schema

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn):entityresolution:.*:[0-9]+:(schemamapping/.*)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ApplyNormalization

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
