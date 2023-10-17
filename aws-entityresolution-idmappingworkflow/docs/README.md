# AWS::EntityResolution::IdMappingWorkflow

IdMappingWorkflow defined in AWS Entity Resolution service

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::EntityResolution::IdMappingWorkflow",
    "Properties" : {
        "<a href="#workflowname" title="WorkflowName">WorkflowName</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#inputsourceconfig" title="InputSourceConfig">InputSourceConfig</a>" : <i>[ <a href="idmappingworkflowinputsource.md">IdMappingWorkflowInputSource</a>, ... ]</i>,
        "<a href="#outputsourceconfig" title="OutputSourceConfig">OutputSourceConfig</a>" : <i>[ <a href="idmappingworkflowoutputsource.md">IdMappingWorkflowOutputSource</a>, ... ]</i>,
        "<a href="#idmappingtechniques" title="IdMappingTechniques">IdMappingTechniques</a>" : <i><a href="idmappingtechniques.md">IdMappingTechniques</a></i>,
        "<a href="#rolearn" title="RoleArn">RoleArn</a>" : <i>String</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::EntityResolution::IdMappingWorkflow
Properties:
    <a href="#workflowname" title="WorkflowName">WorkflowName</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#inputsourceconfig" title="InputSourceConfig">InputSourceConfig</a>: <i>
      - <a href="idmappingworkflowinputsource.md">IdMappingWorkflowInputSource</a></i>
    <a href="#outputsourceconfig" title="OutputSourceConfig">OutputSourceConfig</a>: <i>
      - <a href="idmappingworkflowoutputsource.md">IdMappingWorkflowOutputSource</a></i>
    <a href="#idmappingtechniques" title="IdMappingTechniques">IdMappingTechniques</a>: <i><a href="idmappingtechniques.md">IdMappingTechniques</a></i>
    <a href="#rolearn" title="RoleArn">RoleArn</a>: <i>String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### WorkflowName

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

#### InputSourceConfig

_Required_: Yes

_Type_: List of <a href="idmappingworkflowinputsource.md">IdMappingWorkflowInputSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutputSourceConfig

_Required_: Yes

_Type_: List of <a href="idmappingworkflowoutputsource.md">IdMappingWorkflowOutputSource</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IdMappingTechniques

_Required_: Yes

_Type_: <a href="idmappingtechniques.md">IdMappingTechniques</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RoleArn

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:(aws|aws-us-gov|aws-cn):iam::\d{12}:role/?[a-zA-Z_0-9+=,.@\-_/]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the WorkflowName.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### WorkflowArn

The default IdMappingWorkflow arn

#### UpdatedAt

The time of this IdMappingWorkflow got last updated at

#### CreatedAt

The time of this IdMappingWorkflow got created
