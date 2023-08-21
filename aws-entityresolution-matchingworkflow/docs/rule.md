# AWS::EntityResolution::MatchingWorkflow Rule

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#rulename" title="RuleName">RuleName</a>" : <i>String</i>,
    "<a href="#matchingkeys" title="MatchingKeys">MatchingKeys</a>" : <i>[ String, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#rulename" title="RuleName">RuleName</a>: <i>String</i>
<a href="#matchingkeys" title="MatchingKeys">MatchingKeys</a>: <i>
      - String</i>
</pre>

## Properties

#### RuleName

_Required_: Yes

_Type_: String

_Maximum Length_: <code>255</code>

_Pattern_: <code>^[a-zA-Z_0-9- \t]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MatchingKeys

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
