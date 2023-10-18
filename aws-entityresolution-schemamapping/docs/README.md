# AWS::EntityResolution::SchemaMapping

SchemaMapping defined in AWS Entity Resolution service

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::EntityResolution::SchemaMapping",
    "Properties" : {
        "<a href="#schemaname" title="SchemaName">SchemaName</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#mappedinputfields" title="MappedInputFields">MappedInputFields</a>" : <i>[ <a href="schemainputattribute.md">SchemaInputAttribute</a>, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#hasworkflows" title="HasWorkflows">HasWorkflows</a>" : <i>Boolean</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::EntityResolution::SchemaMapping
Properties:
    <a href="#schemaname" title="SchemaName">SchemaName</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#mappedinputfields" title="MappedInputFields">MappedInputFields</a>: <i>
      - <a href="schemainputattribute.md">SchemaInputAttribute</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#hasworkflows" title="HasWorkflows">HasWorkflows</a>: <i>Boolean</i>
</pre>

## Properties

#### SchemaName

_Required_: Yes

_Type_: String

_Maximum Length_: <code>255</code>

_Pattern_: <code>^[a-zA-Z_0-9-]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Description

_Required_: No

_Type_: String

_Maximum Length_: <code>255</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MappedInputFields

_Required_: Yes

_Type_: List of <a href="schemainputattribute.md">SchemaInputAttribute</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HasWorkflows

The boolean value that indicates whether or not a SchemaMapping has MatchingWorkflows that are associated with

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the SchemaName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### SchemaArn

The SchemaMapping arn associated with the Schema

#### CreatedAt

The time of this SchemaMapping got created

#### UpdatedAt

The time of this SchemaMapping got last updated at
