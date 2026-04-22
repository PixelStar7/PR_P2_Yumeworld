using Assets.Scripts;
using Assets.Scripts.messages;
using MikeSchweitzer.WebSocket;
using System.Collections;
using Unity.VisualScripting;
using UnityEngine;

public class GameControllerScript : MonoBehaviour
{

    public UIScript script;
    public WebSocketConnection _Connection;

    public string _Url = "ws://localhost:8080/ws";

    public bool IsConnected => _Connection.State == WebSocketState.Connected;

    // inline style
    public void Connect()
    {
        _Connection.Connect(_Url);
        _Connection.StateChanged += OnStateChanged;

    }

    private void OnDestroy()
    {
        // Només intentem desubscriure'ns si la connexió existeix
        if (_Connection != null)
        {
            _Connection.StateChanged -= OnStateChanged;
        }
    }


    private void Awake()
    {
        // Connectar al Web Socket
        Connect();

        // Iniciar una coroutine ("peudo-fil") per atendre el web socket

        StartCoroutine(ReceiveMessages());

        // Ens assegurem que l'objecte i la connexió no morin en els canvis 
        // d'escena
        DontDestroyOnLoad(_Connection);
        DontDestroyOnLoad(this.gameObject);
    }

   

    private void OnStateChanged(WebSocketConnection connection, WebSocketState oldState, WebSocketState newState)
    {
        Debug.Log($"OnStateChanged oldState={oldState}|newState={newState}");
        if (newState == WebSocketState.Disconnected)
        {
            // you may change the url here, if you want
            _Connection.Connect();
        }
    }


    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        
    }

    // Update is called once per frame
    private void Update()
    {
       
    }

    /// <summary>
    /// Exemple de funció per enviar un missatge "tonto" al websocket.
    /// </summary>
    public void pickCharacter(int userId, int characterId) { 

        // JSON message és l'embolcall estàndard per tots els missatges.
        JSONMessage<PickCharacterMessage> m = 
            new JSONMessage<PickCharacterMessage>(
                "PICK_PLAYERS",
                "gameidxxxx", 
                new PickCharacterMessage(userId, characterId));


        string json = JsonUtility.ToJson(m);
        Debug.Log("El missatge JSON és:"+json); 
        if (IsConnected) _Connection.AddOutgoingMessage(json);

    }


    /// <summary>
    /// Mètode per gestionar la recepció de missatges.
    /// Cal que llegiu el missatge json i el processeu adequadament
    /// segons cada cas. 
    /// Es recomana usar una altra classe per tal de processar
    /// els missatges i no posar-ho al game controller. 
    /// Per exemple, podeu posar un levelControler i passar-lo per
    /// paràmetre públic.
    /// </summary>
    /// <returns></returns>
    private IEnumerator ReceiveMessages()
    {
        while (true)
        {
            if (_Connection.TryRemoveIncomingMessage(out string message))
            {
                Debug.Log(message);
                script.ShowMessage(message);

            }
            yield return null;
        }
    }

    public void ChooseCharacter(string character)
    {
        Debug.Log("PICKED"+character);
        Message m = new Message(1, character);
        string json = JsonUtility.ToJson(m);
        if (IsConnected) _Connection.AddOutgoingMessage(json);

    }
}
