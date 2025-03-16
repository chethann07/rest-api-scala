package modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.javadsl.Behaviors;

import javax.inject.Inject;

public class ActorSystemProvider extends AbstractModule {

    @Provides
    @Singleton
    @Inject
    public ActorSystem<Void> actorSystem() {
        return ActorSystem.create(Behaviors.empty(), "pekko-actor-system");
    }
}
