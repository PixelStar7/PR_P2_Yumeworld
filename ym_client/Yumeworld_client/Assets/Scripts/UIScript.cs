using System;
using UnityEngine;

public class UIScript : MonoBehaviour
{
    internal void ShowMessage(string message)
    {
        Debug.Log("Missatge rebut a la UI: " + message);
        // Aquí podries posar: elMeuText.text = message;
    }

    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {

    }

    // Update is called once per frame
    void Update()
    {

    }
}

[System.Serializable]
public class Message
{
    public int id;
    public string content;

    public Message(int id, string content)
    {
        this.id = id;
        this.content = content;
    }
}
