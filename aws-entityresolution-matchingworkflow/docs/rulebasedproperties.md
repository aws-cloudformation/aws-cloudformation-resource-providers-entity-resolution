# AWS::EntityResolution::MatchingWorkflow RuleBasedProperties

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#rules" title="Rules">Rules</a>" : <i>[ <a href="rule.md">Rule</a>, ... ]</i>,
    "<a href="#attributematchingmodel" title="AttributeMatchingModel">AttributeMatchingModel</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#rules" title="Rules">Rules</a>: <i>
      - <a href="rule.md">Rule</a></i>
<a href="#attributematchingmodel" title="AttributeMatchingModel">AttributeMatchingModel</a>: <i>String</i>
</pre>

## Properties

#### Rules

_Required_: Yes

_Type_: List of <a href="rule.md">Rule</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AttributeMatchingModel

_Required_: Yes

_Type_: String

_Allowed Values_: <code>ONE_TO_ONE</code> | <code>MANY_TO_MANY</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
