# AWS::EntityResolution::MatchingWorkflow ResolutionTechniques

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#resolutiontype" title="ResolutionType">ResolutionType</a>" : <i>String</i>,
    "<a href="#rulebasedproperties" title="RuleBasedProperties">RuleBasedProperties</a>" : <i><a href="rulebasedproperties.md">RuleBasedProperties</a></i>
}
</pre>

### YAML

<pre>
<a href="#resolutiontype" title="ResolutionType">ResolutionType</a>: <i>String</i>
<a href="#rulebasedproperties" title="RuleBasedProperties">RuleBasedProperties</a>: <i><a href="rulebasedproperties.md">RuleBasedProperties</a></i>
</pre>

## Properties

#### ResolutionType

_Required_: No

_Type_: String

_Allowed Values_: <code>RULE_MATCHING</code> | <code>ML_MATCHING</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RuleBasedProperties

_Required_: No

_Type_: <a href="rulebasedproperties.md">RuleBasedProperties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
