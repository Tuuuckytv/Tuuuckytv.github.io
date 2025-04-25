#Requires AutoHotkey v2.0
#SingleInstance Force

; --- Prompt Lists ---
prompts := [
    "I’ll ensure it keeps progressing without any hiccups.",
    "I’ll keep everything moving forward as smoothly as possible.",
    "I’ll guarantee it continues to run smoothly.",
    "I’ll make sure things keep advancing without any delays.",
    "I’ll oversee the process to ensure smooth progress.",
    "I’ll keep things on track for seamless progression.",
    "I’ll ensure everything stays on course and runs smoothly.",
    "I’ll monitor it to keep things moving efficiently.",
    "I’ll ensure there are no interruptions as things progress.",
    "I’ll make sure the process stays on track without delays.",
    "I’ll make sure it advances without any obstacles.",
    "I’ll keep it flowing smoothly with no disruptions.",
    "I’ll ensure it progresses without any setbacks.",
    "I’ll monitor it closely to guarantee smooth progress.",
    "I’ll ensure everything keeps moving forward as planned.",
    "I’ll stay on top of it to ensure smooth and uninterrupted progress.",
    "I’ll ensure there are no bumps in the road as it progresses.",
    "I’ll keep everything moving forward without delays or issues.",
    "I’ll ensure smooth progression without any hold-ups.",
    "I’ll make sure it continues advancing without any disruptions.",
    "I’ll oversee things to make sure they progress seamlessly.",
    "I’ll ensure the process remains smooth and uninterrupted.",
    "I’ll guarantee steady and smooth progress without delays.",
    "I’ll monitor the situation to ensure smooth, continuous progress.",
    "I’ll keep things on track to avoid any delays or obstacles.",
    "I’ll make sure everything keeps moving forward smoothly.",
    "I’ll ensure the process remains efficient and uninterrupted.",
    "I’ll stay on top of everything to keep things moving smoothly.",
    "I’ll guarantee no disruptions as things move forward.",
    "I’ll make sure progress continues effortlessly and without delay.",
    "I’ll ensure it moves through as planned.",
    "I’ll keep an eye on it to ensure everything stays on track and progresses as expected. If anything changes or requires attention, I’ll be sure to follow up.",
    "Thanks again for flagging — always helpful."
]

yesPrompts := [
    "Absolutely, I can take care of that.",
    "I'll keep an eye on it",
    "No problem  I’ll sort it.",
    "Yes, sorted",
    "Sounds good, I’ll get on it.",
    "Of course, I’ll make sure it’s done.",
    "Yep, I can do that for you.",
    "Sure, I’ll take care of it ASAP.",
    "Alright, I’ll get started on that.",
    "Okay, consider it done."
]

noPrompts := [
    "That’s not something we currently offer.",
    "Unfortunately, we don’t support that.",
    "Not here sorry.",
    "Sorry, that’s outside the scope of what we provide.",
    "We’re not set up to do that at the moment.",
    "That’s not a service we handle, unfortunately.",
    "I’m afraid that’s not something we can accommodate.",
    "That’s beyond what we’re able to do right now.",
    "Regrettably, we can’t take that on.",
    "That's not something we're equipped to deliver.",
    "Unfortunately, that's not part of our process."
]

adhdPrompts := ["They still need to go through the clinical check process",
"Order was Archived on - ",
"This was already labelled, so it should go within the next 24",
"I have cancelled this",
"We haven't received this yet.",
]
button7Prompts := ["Will get it reprocessed.", 
"Will do.",
"It should be processed within the next 24 to 48 hours.",
]
button8Prompts := ["Everything has been fully picked; it's now just waiting to be checked.",
]
button9Prompts := ["The order has been shipped and is on its way.",
"The order has been dispatched.",
"The order has been shipped out."
]
button10Prompts := []
button11Prompts := []
button12Prompts := []
button13Prompts := []
button14Prompts := []
button15Prompts := []
button16Prompts := []
button17Prompts := []
button18Prompts := []

; --- Shuffle State Management ---
shuffledData := Map()

ShufflePromptList(name, originalList) {
    shuffled := originalList.Clone()
    Loop shuffled.Length {
        i := A_Index
        j := Random(1, shuffled.Length)
        temp := shuffled[i]
        shuffled[i] := shuffled[j]
        shuffled[j] := temp
    }
    shuffledData[name] := { list: shuffled, index: 1 }
}

GetNextPrompt(name, originalList) {
    global shuffledData
    if !shuffledData.Has(name) || shuffledData[name].index > shuffledData[name].list.Length {
        ShufflePromptList(name, originalList)
    }
    prompt := shuffledData[name].list[shuffledData[name].index]
    shuffledData[name].index++
    return prompt
}

; --- GUI Setup ---
myGui := Gui("+AlwaysOnTop", "Prompt Generator")
myGui.SetFont("s10")
editBox := myGui.AddText("w400 h80 Center", "")

generateBtn := myGui.AddButton("w120", "Generate Prompt")
copyBtn := myGui.AddButton("x+10 w120", "Clear")
pasteBtn := myGui.AddButton("x+10 w120", "Paste")

yesBtn := myGui.AddButton("xm y+10 w120", "Yes")
noBtn := myGui.AddButton("x+10 w120", "No")
adhdBtn := myGui.AddButton("x+10 w120", "ADHD")

btn7 := myGui.AddButton("xm y+10 w120", "Kyle-isms")
btn8 := myGui.AddButton("x+10 w120", "NHS")
btn9 := myGui.AddButton("x+10 w120", "Shipped")

btn10 := myGui.AddButton("xm y+10 w120", "Custom 4")
btn11 := myGui.AddButton("x+10 w120", "Custom 5")
btn12 := myGui.AddButton("x+10 w120", "Custom 6")

btn13 := myGui.AddButton("xm y+10 w120", "Custom 7")
btn14 := myGui.AddButton("x+10 w120", "Custom 8")
btn15 := myGui.AddButton("x+10 w120", "Custom 9")

btn16 := myGui.AddButton("xm y+10 w120", "Set Paste")
btn17 := myGui.AddButton("x+10 w120", "Custom 11")
btn18 := myGui.AddButton("x+10 w120", "Custom 12")

; --- Global paste position (percentage of primary screen) ---
global pasteXPercent := 0.8
global pasteYPercent := 0.8

; --- Event Bindings ---
generateBtn.OnEvent("Click", (*) => (editBox.Value := prompts.Length ? GetNextPrompt("main", prompts) : "No prompts available"))
copyBtn.OnEvent("Click", (*) => (editBox.Value := ""))
pasteBtn.OnEvent("Click", (*) => PastePrompt())

yesBtn.OnEvent("Click", (*) => (editBox.Value := yesPrompts.Length ? GetNextPrompt("yes", yesPrompts) : "No 'Yes' prompts available"))
noBtn.OnEvent("Click", (*) => (editBox.Value := noPrompts.Length ? GetNextPrompt("no", noPrompts) : "No 'No' prompts available"))
adhdBtn.OnEvent("Click", (*) => (editBox.Value := adhdPrompts.Length ? GetNextPrompt("adhd", adhdPrompts) : "No ADHD prompts available"))

btn7.OnEvent("Click", (*) => (editBox.Value := button7Prompts.Length ? GetNextPrompt("btn7", button7Prompts) : "No Kyle-isms prompts available"))
btn8.OnEvent("Click", (*) => (editBox.Value := button8Prompts.Length ? GetNextPrompt("btn8", button8Prompts) : "No Custom 2 prompts available"))
btn9.OnEvent("Click", (*) => (editBox.Value := button9Prompts.Length ? GetNextPrompt("btn9", button9Prompts) : "No Custom 3 prompts available"))
btn10.OnEvent("Click", (*) => (editBox.Value := button10Prompts.Length ? GetNextPrompt("btn10", button10Prompts) : "No Custom 4 prompts available"))
btn11.OnEvent("Click", (*) => (editBox.Value := button11Prompts.Length ? GetNextPrompt("btn11", button11Prompts) : "No Custom 5 prompts available"))
btn12.OnEvent("Click", (*) => (editBox.Value := button12Prompts.Length ? GetNextPrompt("btn12", button12Prompts) : "No Custom 6 prompts available"))
btn13.OnEvent("Click", (*) => (editBox.Value := button13Prompts.Length ? GetNextPrompt("btn13", button13Prompts) : "No Custom 7 prompts available"))
btn14.OnEvent("Click", (*) => (editBox.Value := button14Prompts.Length ? GetNextPrompt("btn14", button14Prompts) : "No Custom 8 prompts available"))
btn15.OnEvent("Click", (*) => (editBox.Value := button15Prompts.Length ? GetNextPrompt("btn15", button15Prompts) : "No Custom 9 prompts available"))
btn16.OnEvent("Click", SetCustomPastePosition)
btn17.OnEvent("Click", (*) => (editBox.Value := button17Prompts.Length ? GetNextPrompt("btn17", button17Prompts) : "No Custom 11 prompts available"))
btn18.OnEvent("Click", (*) => (editBox.Value := button18Prompts.Length ? GetNextPrompt("btn18", button18Prompts) : "No Custom 12 prompts available"))

; --- Functions ---
PastePrompt() {
    global pasteXPercent, pasteYPercent
    A_Clipboard := RegExReplace(Trim(editBox.Value), "\.\w+$", "")
    Sleep(400)
    CoordMode("Mouse", "Screen")
    pos := GetAbsolutePositionFromPercent(pasteXPercent, pasteYPercent)
    x := pos[1]
    y := pos[2]
    MouseMove(x, y, 0)
    Click()
    Sleep(400)
    Send("^!x")
    Sleep(400)
    Send("^v")
}

GetAbsolutePositionFromPercent(xPercent, yPercent) {
    screenWidth := A_ScreenWidth
    screenHeight := A_ScreenHeight
    return [Round(screenWidth * xPercent), Round(screenHeight * yPercent)]
}

SetCustomPastePosition(*) {
    static locked := false
    if locked {
        MsgBox("Paste location already set.")
        return
    }
    tempGui := Gui("-SysMenu +AlwaysOnTop", "Set Paste Location")
    tempGui.SetFont("s10")
    tempGui.AddText("w300", "Click anywhere to set the paste location...")
    tempGui.Show("AutoSize Center")
    SetTimer(WatchMouseClick, 10)

    WatchMouseClick(*) {
        static clicked := false
        if (GetKeyState("LButton", "P") && !clicked) {
            clicked := true
            CoordMode("Mouse", "Screen")  ; Ensure we are using screen coordinates
            MouseGetPos(&x, &y)
            global pasteXPercent := x / A_ScreenWidth
            global pasteYPercent := y / A_ScreenHeight
            ToolTip("Paste position set to: " pasteXPercent ", " pasteYPercent)
            Sleep(1000)
            ToolTip()  ; Remove the tooltip after 1 second
            SetTimer(WatchMouseClick, 0)  ; Stop the timer after the click is recorded
            tempGui.Destroy()  ; Destroy the temporary GUI
            locked := true
            btn16.Enabled := false  ; Disable the "Set Paste Location" button after setting the position
        }
    }
}

; --- Show GUI ---
myGui.Show()
