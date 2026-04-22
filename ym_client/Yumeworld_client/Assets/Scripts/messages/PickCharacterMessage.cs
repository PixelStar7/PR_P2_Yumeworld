using System;
using System.Collections.Generic;
using System.Text;

namespace Assets.Scripts.messages
{
    [System.Serializable]
    internal class PickCharacterMessage
    {

        public int playerId;
        public int characterId;

        public PickCharacterMessage()
        {
        }

        public PickCharacterMessage(int playerId, int characterId)
        {
            this.playerId = playerId;
            this.characterId = characterId;
        }
    }
}
