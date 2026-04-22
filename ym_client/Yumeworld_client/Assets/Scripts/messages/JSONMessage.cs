using System;
using System.Collections.Generic;
using System.Text;

namespace Assets.Scripts.messages
{
    [System.Serializable]
    internal class JSONMessage<T>
    {
        public String messageType;
        public String gameId;
        public T data; // node genèric (pot ser qualevol objecte complex)

        public JSONMessage() { }
        public JSONMessage(string messageType, string gameId, T data)
        {
            this.messageType = messageType;
            this.gameId = gameId;
            this.data = data;
        }
    }
}
