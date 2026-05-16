---
description: Graceful session handoff - capture context for a fresh conversation
---

# /handoff - Session Transition

Use this when a session is getting long or unstable. Creates a lightweight summary to paste into a fresh session.

## Steps

1. **Capture current state** (DO NOT read full files):
   - Check recent artifacts in brain folder
   - Note any in-progress tasks
   - List files that were being worked on 

2. **Generate handoff block** for the user to copy:
Session Handoff - [DATE]

Where We Left Off
[1-2 sentence summary of what was happening]
In Progress
Key Files (do NOT read yet)
[file1.ts] - [why relevant]
[file2.tsx] - [why relevant]
Next Action
[What to do first in the new session]
3. **Output the block** so user can copy it.
4. **Remind user**:
- Close this session
- Open fresh session
- Paste the handoff block
- Say "continue" or "let's pick up here"
Advice for Outages & "Model Hopping"
When the system is in an error state or tools are failing, the goal is Context Preservation with Minimal Friction. Here is how to handle it:
Minimalism is Key: When the model is struggling, don't ask it to "write code" or "debug." Every tool call is a potential point of failure. Instead, use a single turn to ask: "Just give me a /handoff  summary so I can switch models." I just type /handoff
Model Hopping: If one model is hitting errors or getting stuck in loops, switch to another model (e.g., jump from Claude to Gemini or vice-versa) and type /handoff  . The new model will have a fresh "Internal Context Window" but still know exactly where the project stands.
The "Stateless" Mindset: Treat every session as temporary. If you get a single good response during an outage, immediately ask for a summary. Don't wait for the "perfect moment" to save your progress.
Retrieve, Don't Process: If a model is failing the "Execution" but can still "Read," just ask it to output the last few lines of what it was working on so you can manually copy it to your notes.