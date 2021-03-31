# Table of Contents

1. [Structure](#structure)
   1. [Activity](#activity)
   2. [Fragment](#fragment)
      1. [HomeFragment](#HomeFragment)
      2. [MessageFragment](#MessageFragment)
      3. [OrderFragment](#OrderFragment)
      4. [ProfileFragment](#ProfileFragment)
   3. [Bean](#bean)
   4. [Service](#service)
   5. [Tool](#tool)

<a id="structure"></a>

# Structure

<a id="activity"></a>

## Activity

1. AssistantLocationActivity

   For map api invoke

2. LoginActivity

   For login service

3. MainActivity

   Main frame of DuoDuoPin, has 4 fragment respectively are [HomeFragment](#HomeFragment), [MessageFragment](#MessageFragment), [OrderFragment](#OrderFragment) and [ProfileFragment](#ProfileFragment).

4. OneGrpMsgCaseActivity

   Sperate GrpMsgCase for all GrpMsgCases.

5. OneOrderCaseActivity

   Sperate OrderCase for all OrderCases.

6. OneSysMsgCaseActivity

   Sperate SysMsgCase for all SysMsgCases.

7. OrderCaseActivity

   Show OrderCases for different search conditions.

8. RegisterActivity

   Provide register service.

9. SysMsgCaseActivity

   Show all SysMsgCases.

<a id="fragment"></a>

## Fragment

<a id="HomeFragment"></a>

### HomeFragment

Main content is a side menu consists of different search utilities.

<a id="MessageFragment"></a>

### MessageFragment

Top of it is a ConstraintLayout used to enter SysMsgCaseActivity.

Bottom of it are all brief group message tips which display latest message brief information in a ListView.

<a id="OrderFragment"></a>

### OrderFragment

A new order's entrance.

Fragment for input information about a order which can be submitted to the database.

<a id="ProfileFragment"></a>

### ProfileFragment

Consists of 4 buttons used to get user's own bill and car orders, to clear app's cache, to logout.

<a id="bean"></a>

## Bean

1. BriefGrpMsg

   Bean to display brief information for every group in MessageFragment.

2. GrpMsgContent

   Bean to receive group message information from server and other people.

3. GrpMsgDisplay

   Bean to fill every single message in OneGrpMsgCaseActivity.

4. OrderContent

   Bean to receive order message information from server.

5. SysMsgContent

   Bean to receive system message information from server.

<a id="service"></a>

## Service

1. RecMsgService

   service to receive new group messages in background.

<a id="tool"></a>

## Tool

1. Constants

   All of urls used to communicate with server.

2. GrpMsgAdapter

   Adapter for seperate one group message UI.

3. MapInvoker

   To invoke map service.

4. MyDBHelper

   My DB helper to create and upgrade tables.
