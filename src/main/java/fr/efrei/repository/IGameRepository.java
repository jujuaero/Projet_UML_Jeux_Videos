package fr.efrei.repository;

import fr.efrei.domain.Game;
import fr.efrei.domain.GamePlatform;
import fr.efrei.domain.GameType;
import java.util.List;

public interface IGameRepository extends IRepository<Game> {

    List<Game> findByPlatformAndType(GamePlatform platform, GameType type);
}

