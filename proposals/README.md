# Proposals

Marquez uses a _multi_-project structure and contains the modules [`api`](https://github.com/MarquezProject/marquez/tree/main/api), [`web`](https://github.com/MarquezProject/marquez/tree/main/web), [`clients`](https://github.com/MarquezProject/marquez/tree/main/clients), and [`chart`](https://github.com/MarquezProject/marquez/tree/main/chart). Below, we describe the process for proposing, documenting, and implementing changes to Marquez.


## Submitting a Proposal for Review

1. Open a [new issue](https://github.com/MarquezProject/marquez/issues/new) briefly describing your proposal
2. Work with Marquez [committers](https://github.com/MarquezProject/marquez/blob/main/COMMITTERS.md) to get your proposal reviewed

   > **Note:** Your proposal can have one of two outcomes: _`accepted`_ or _`decline`_; if the proposal is _`declined`_, the process is done.

3. If the proposal is _`accepted`_, it will be added to our [Backlog](https://github.com/orgs/MarquezProject/projects/1); the proposal author **must** write a design doc using our [template](https://github.com/MarquezProject/marquez/blob/main/proposals/TEMPLATE.md) for new proposals

   > **Note:** Your proposal **must** be added under [`proposals/`]() using the naming convention: `[#ISSUE]-[SHORT-NAME].md`, where `[#ISSUE]` is the GitHub issue in **Step 1**, and `[SHORT-NAME]` is a shortened name of your proposal (each word seperated by dashes (`-`)).

4. Open a [pull request](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md#submitting-a-pull-request) with your proposal for final discussion; once Marquez [committers](https://github.com/MarquezProject/marquez/blob/main/COMMITTERS.md) reach a general consensus on your proposal, it will be added to a [Milestone](https://github.com/MarquezProject/marquez/milestones)

Once your proposal has been _`accepted`_, and has been associated with a milestone, you can begin implementation work by following our [contributing](https://github.com/MarquezProject/marquez/blob/main/CONTRIBUTING.md) guide for Marquez.

## Questions?

If you need help with the proposal process, please reach out to us on our [slack](http://bit.ly/MarquezSlack) channel.
