package org.codequistify.master.core.domain.player.port;

import org.codequistify.master.core.domain.player.model.Player;
import org.codequistify.master.core.domain.player.model.PolId;

public interface PlayerReader {
    Player findOneByUid(PolId uid);
}
