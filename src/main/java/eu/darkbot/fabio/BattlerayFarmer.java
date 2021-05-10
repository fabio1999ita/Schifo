package eu.darkbot.fabio;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.BoxInfo;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.NpcInfo;
import com.github.manolo8.darkbot.config.types.Option;
import com.github.manolo8.darkbot.core.entities.Box;
import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.entities.Npc;
import com.github.manolo8.darkbot.core.entities.Portal;
import com.github.manolo8.darkbot.core.itf.Configurable;
import com.github.manolo8.darkbot.core.itf.Module;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.Map;
import com.github.manolo8.darkbot.core.utils.Location;
import com.github.manolo8.darkbot.extensions.features.Feature;
import com.github.manolo8.darkbot.modules.LootNCollectorModule;
import com.github.manolo8.darkbot.modules.MapModule;
import com.github.manolo8.darkbot.modules.utils.NpcAttacker;
import eu.darkbot.VerifierChecker.VerifierChecker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static com.github.manolo8.darkbot.Main.API;

@Feature(name = "BattlerayFarmer", description = "this module was made to farm Battlerays in 5-3", enabledByDefault = false)
public class BattlerayFarmer extends LootNCollectorModule implements Configurable<BattlerayFarmer.BattlerayConfig> {
    public List<Portal> portals;
    private Map MAP;
    private Main main;
    private HeroManager hero;
    private List<Npc> npcs;
    private List<Box> boxes;
    private NpcAttacker attack;
    private Npc BATTLERAY;
    private Npc INTERCEPTOR;
    private Npc SABOTEUR;
    private Portal SAFE;
    private Box PALLADIUM;
    private BattlerayConfig battlerayConfig;
    private State currentStatus;
    private boolean isRepaired = true;
    private Config config;

    public void install(Main main) {
        if (!Arrays.equals(VerifierChecker.class.getSigners(), getClass().getSigners())) return;
        if (!VerifierChecker.getAuthApi().requireDonor()) return;
        MAP = main.starManager.byName("5-3");
        this.main = main;
        npcs = main.mapManager.entities.npcs;
        portals = main.mapManager.entities.portals;
        attack = new NpcAttacker(main);
        this.hero = main.hero;
        this.currentStatus = State.IDLE;
        this.boxes = main.mapManager.entities.boxes;
        this.config = main.config;
        preSet();
    }

    public void uninstall() {

    }

    @Override
    public boolean canRefresh() {
        return false;
    }

    public void tickModule() {
        if (main.hero.map != null)
            if (main.hero.map != this.MAP) {
                ((MapModule) this.main.setModule((Module) new MapModule())).setTarget(this.MAP);
                main.hero.roamMode();
            } else {
                BATTLERAY = this.npcs.stream().filter(npc -> npc.playerInfo.username.equals("-=[ Battleray ]=-"))
                        .min(Comparator.comparingDouble(npc -> npc.locationInfo.distance(this.hero))).orElse(null);

                INTERCEPTOR = this.npcs.stream().filter(npc -> npc.playerInfo.username.equals("-=[ Interceptor ]=-"))
                        .findFirst().orElse(null);

                SABOTEUR = this.npcs.stream().filter(npc -> npc.playerInfo.username.equals("-=[ Saboteur ]=-"))
                        .findFirst().orElse(null);

                SAFE = this.portals.stream().filter(p -> p.factionId == main.hero.playerInfo.factionId)
                        .min(Comparator.comparingDouble(p -> p.locationInfo.distance(this.hero))).orElse(null);

                if (SAFE != null)
                    if (getDistance(SAFE) < 200)
                        isRepaired = hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max;

                if (BATTLERAY != null && (hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && isRepaired)) {
                    if (getDistance(BATTLERAY) > 1400)
                        currentStatus = State.DRIVE_BATTLERAY;
                    else
                        currentStatus = State.DRIVE_BATTLERAY_ATTACK;
                    if (getDistance(BATTLERAY) > 2200 && INTERCEPTOR != null && battlerayConfig.killAlienWhenFly) {
                        if (hero.shipInfo.speed < 200 && getDistance(SABOTEUR) < 1000 && battlerayConfig.killSaboteur) {
                            main.hero.attackMode();
                            main.hero.drive.move(BATTLERAY.locationInfo.now);
                            attack.target = SABOTEUR;
                            attack.doKillTargetTick();
                        } else {
                            main.hero.roamMode();
                            main.hero.drive.move(BATTLERAY.locationInfo.now);
                            attack.target = INTERCEPTOR;
                            attack.doKillTargetTick();
                            if (attack.hasTarget() && attack.target.locationInfo.now.distance(hero.locationInfo.now) > 950)
                                attack.target = null;
                        }
                    } else {
                        if ((main.hero.target == null || !attack.hasTarget()) && (getDistance(BATTLERAY) > 1000)) {
                            if (getDistance(BATTLERAY) > 1600)
                                main.hero.roamMode();
                            main.hero.drive.move(BATTLERAY.locationInfo.now);
                        } else {
                            if (INTERCEPTOR != null && BATTLERAY.ish) {
                                if (getDistance(INTERCEPTOR) > 700)
                                    main.hero.drive.move(INTERCEPTOR.locationInfo.now);
                                currentStatus = State.ATTACK_INTERCEPTOR;
                                startAttack(INTERCEPTOR);
                                if (attack.hasTarget() && attack.target.locationInfo.now.distance(hero.locationInfo.now) > 950)
                                    attack.target = null;
                                if (attack.hasTarget())
                                    moveLogic();
                            }
                            if (BATTLERAY != null && (INTERCEPTOR == null || !BATTLERAY.ish)) {
                                if (getDistance(BATTLERAY) > 700)
                                    main.hero.drive.move(BATTLERAY.locationInfo.now);
                                currentStatus = State.ATTACK_BATTLERAY;
                                startAttack(BATTLERAY);
                                if (attack.hasTarget())
                                    moveLogic();
                            }
                        }
                    }
                } else {
                    if (!(hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max) || !(battlerayConfig.pickPalladium)) {
                        if (hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.min && hero.health.hpPercent() > main.config.GENERAL.SAFETY.REPAIR_HP_RANGE.max)
                            currentStatus = State.DRIVE_SAFE;
                        else
                            currentStatus = State.DRIVE_SAFE_REPAIR;
                        if (SAFE != null && getDistance(SAFE) > 200) {
                            main.hero.roamMode();
                            hero.drive.move(SAFE);
                        }
                        if (!hero.drive.isMoving() && hero.health.shieldPercent() >= 0.85D && getDistance(SAFE) < 200) {
                            hero.setMode(main.config.GENERAL.SAFETY.REPAIR);
                            if (System.currentTimeMillis() - main.lastRefresh > ((long) config.MISCELLANEOUS.REFRESH_TIME - 0.2) * 60 * 1000)
                                API.handleRefresh();
                        }
                    } else {
                        PALLADIUM = this.boxes.stream().filter(box -> box.type.equals("ore_8")).min(Comparator.comparingDouble(box -> this.hero.locationInfo.now.distance(box))).orElse(null);
                        if (!main.hero.drive.isMoving() || PALLADIUM != null) {
                            currentStatus = State.DRIVE_PALLADIUM;
                            main.hero.roamMode();

                            Random r = new Random();
                            int leftX = 12900, rightX = 32200;
                            int upY = 18400, downY = 25500;
                            double randX = r.nextInt(rightX - leftX) + leftX;
                            double randY = r.nextInt(downY - upY) + upY;
                            if (PALLADIUM == null) {
                                hero.drive.move(randX, randY);
                            } else {
                                if (collectorModule.isNotWaiting()) {
                                    collectorModule.findBox();
                                    collectorModule.tryCollectNearestBox();
                                }
                            }
                        }
                    }
                }
            }
    }

    private void preSet() {
        config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Battleray ]=-", n -> new NpcInfo()).radius = 630;
        config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Interceptor ]=-", n -> new NpcInfo()).radius = 580;
        config.LOOT.NPC_INFOS.computeIfAbsent("-=[ Saboteur ]=-", n -> new NpcInfo()).radius = 580;
        config.COLLECT.BOX_INFOS.computeIfAbsent("ore_8", n -> new BoxInfo()).collect = true;
        config.COLLECT.BOX_INFOS.computeIfAbsent("ore_8", n -> new BoxInfo()).waitTime = 780;
        config.GROUP.OPEN_INVITES = true;
        config.GROUP.ACCEPT_INVITES = true;
    }

    private double getDistance(Entity entity) {
        return hero.locationInfo.now.distance(entity.locationInfo.now);
    }

    private void startAttack(Npc npc) {
        if (npc != null) {
            attack.target = npc;
            main.hero.attackMode(npc);
            attack.doKillTargetTick();
        }
    }

    public String status() {
        return currentStatus.message;
    }

    private void moveLogic() {
        Npc target = this.attack.target;
        if (target != null && target.locationInfo != null) {
            Location heroLoc = main.hero.locationInfo.now;
            Location targetLoc = target.locationInfo.destinationInTime(400L);
            double angle = targetLoc.angle(heroLoc);
            double radius = target.npcInfo.radius;
            double distance = radius;

            angle += Math.max((double) this.hero.shipInfo.speed * 0.625D + Double.min(200.0D, target.locationInfo.speed) * 0.625D - heroLoc.distance(Location.of(targetLoc, angle, radius)), 0.0D) / radius;
            Location direction = Location.of(targetLoc, angle, distance);

            while (!main.hero.drive.canMove(direction) && distance < 10000.0D) {
                direction.toAngle(targetLoc, angle += 0.3D, distance += 2.0D);
            }

            if (distance >= 10000.0D) {
                direction.toAngle(targetLoc, angle, 500.0D);
            }

            main.hero.drive.move(direction);
        }
    }

    public void setConfig(BattlerayConfig battlerayConfig) {
        this.battlerayConfig = battlerayConfig;
    }

    private enum State {
        IDLE("Idle"),
        DRIVE_BATTLERAY("Driving to Battleray"),
        DRIVE_BATTLERAY_ATTACK("Driving to Battleray, while attacking"),
        ATTACK_BATTLERAY("Killing Battleray"),
        ATTACK_INTERCEPTOR("Killing Interceptor"),
        DRIVE_SAFE("Driving to safe spot"),
        DRIVE_SAFE_REPAIR("Driving to safe spot for repair"),
        DRIVE_PALLADIUM("Searching Palladium");

        private final String message;

        State(String message) {
            this.message = message;
        }
    }

    public static class BattlerayConfig {
        @Option(value = "Kill alien when fly", description = "Kill alien when fly")
        public boolean killAlienWhenFly = true;

        @Option(value = "Pick palladium", description = "Pick palladium while waiting Battleray")
        public boolean pickPalladium = false;

        @Option(value = "Kill saboteur", description = "Kill saboteur if it's slowing you down (need kill alien when fly on)")
        public boolean killSaboteur = false;
    }

}