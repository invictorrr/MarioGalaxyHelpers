# MARIO GALAXY HELPERS - MOD FORGE 1.20.1

## Documento Técnico para Desarrollo

**Tipo:** Mod de Minecraft Forge  
**Versión:** Minecraft 1.20.1  
**Forge:** 47.2.0+  
**Java:** 17

---

## DESCRIPCIÓN

Mod que permite crear un mapa donde el jugador "contrata" NPCs de Mario Galaxy que le ayudan a pasar Minecraft. Cada NPC tiene un coste en coins y una habilidad única.

### Dependencias Runtime
- `super_mario-1.1.5-forge-1.20.1.jar` (items, entidades, estructuras)
- `super_block_world-0.0.8.jar` (monedas)
- CustomPlayerModels (opcional, para modelos custom)

### Items de los Mods que Usamos
```
super_block_world:coin
super_mario:yoshi_egg_* (green, blue, red, etc.)
super_mario:star_bit
super_mario:power_star
super_mario:question_mark_block
super_mario:goomba (entidad)
super_mario:koopa_green (entidad)
super_mario:kamek (entidad)
super_mario:boom_boom (entidad)
super_mario:yoshi (entidad montable)
```

---

## ESTRUCTURA DEL PROYECTO

```
mario-galaxy-helpers/
├── build.gradle
├── gradle.properties
├── settings.gradle
├── src/main/
│   ├── java/com/mariogalaxyhelpers/
│   │   ├── MarioGalaxyHelpers.java
│   │   ├── capability/
│   │   │   ├── PlayerData.java
│   │   │   ├── PlayerDataProvider.java
│   │   │   └── ModCapabilities.java
│   │   ├── entity/
│   │   │   ├── NPCEntity.java (clase base)
│   │   │   ├── ToadEntity.java
│   │   │   ├── YoshiPlaceholderEntity.java
│   │   │   ├── LuigiEntity.java
│   │   │   ├── CaptainToadEntity.java
│   │   │   ├── RosalinaEntity.java
│   │   │   ├── LumaEntity.java
│   │   │   ├── PeachEntity.java
│   │   │   └── BowserJrEntity.java
│   │   ├── registry/
│   │   │   └── ModEntities.java
│   │   ├── event/
│   │   │   ├── ModEvents.java
│   │   │   └── ItemPickupEvents.java
│   │   ├── client/
│   │   │   ├── CoinHudOverlay.java
│   │   │   └── renderer/
│   │   └── command/
│   │       └── ModCommands.java
│   └── resources/
│       ├── META-INF/mods.toml
│       └── assets/mariogalaxyhelpers/
│           ├── textures/entity/
│           └── lang/en_us.json
```

---

## SISTEMA DE COINS - CAPABILITY

### PlayerData.java

```java
public class PlayerData {
    private int coins = 0;
    private Set<String> hiredNPCs = new HashSet<>();
    private int killCount = 0;
    private int eggsCollected = 0;
    private int starBitsCollected = 0;
    private int powerStarsCollected = 0;
    
    // Coins
    public int getCoins() { return coins; }
    public void addCoins(int amount) { coins += amount; }
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    // NPCs contratados
    public boolean isHired(String npcId) { return hiredNPCs.contains(npcId); }
    public void hire(String npcId) { hiredNPCs.add(npcId); }
    public int getHiredCount() { return hiredNPCs.size(); }
    
    // Misiones
    public void incrementKills() { killCount++; }
    public int getKillCount() { return killCount; }
    public void incrementPowerStars() { powerStarsCollected++; }
    
    // NBT serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("coins", coins);
        tag.putInt("killCount", killCount);
        tag.putInt("eggsCollected", eggsCollected);
        tag.putInt("starBitsCollected", starBitsCollected);
        tag.putInt("powerStarsCollected", powerStarsCollected);
        
        ListTag hiredList = new ListTag();
        for (String npc : hiredNPCs) {
            hiredList.add(StringTag.valueOf(npc));
        }
        tag.put("hiredNPCs", hiredList);
        return tag;
    }
    
    public void deserializeNBT(CompoundTag tag) {
        coins = tag.getInt("coins");
        killCount = tag.getInt("killCount");
        eggsCollected = tag.getInt("eggsCollected");
        starBitsCollected = tag.getInt("starBitsCollected");
        powerStarsCollected = tag.getInt("powerStarsCollected");
        
        hiredNPCs.clear();
        ListTag hiredList = tag.getList("hiredNPCs", Tag.TAG_STRING);
        for (int i = 0; i < hiredList.size(); i++) {
            hiredNPCs.add(hiredList.getString(i));
        }
    }
}
```

### PlayerDataProvider.java

```java
public class PlayerDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerData> PLAYER_DATA = CapabilityManager.get(new CapabilityToken<>(){});
    
    private PlayerData data = null;
    private final LazyOptional<PlayerData> optional = LazyOptional.of(this::createData);
    
    private PlayerData createData() {
        if (data == null) data = new PlayerData();
        return data;
    }
    
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap == PLAYER_DATA) return optional.cast();
        return LazyOptional.empty();
    }
    
    @Override
    public CompoundTag serializeNBT() { return createData().serializeNBT(); }
    
    @Override
    public void deserializeNBT(CompoundTag nbt) { createData().deserializeNBT(nbt); }
}
```

---

## NPCs - ESPECIFICACIÓN

### Tabla Resumen

| # | NPC | ID | Coste | Habilidad | Tipo | Cooldown |
|---|-----|-----|-------|-----------|------|----------|
| 1 | Toad | `toad` | 10 | Caza animales → comida cocinada | AUTO | 5s |
| 2 | Yoshi | `yoshi` | 50 | Montura (usa entidad del mod) | ESPECIAL | - |
| 3 | Luigi | `luigi` | 200 | Desaparece 15s → vuelve con hierro | CLICK | 30s |
| 4 | Captain Toad | `captain_toad` | 500 | Muestra partículas hacia estructura | CLICK | 60s |
| 5 | Rosalina | `rosalina` | 1000 | Menas cercanas emiten partículas | AUTO | 2s |
| 6 | Luma | `luma` | 2500 | Mobs hostiles cercanos arden | AUTO | 30s |
| 7 | Peach | `peach` | 5000 | Regeneración constante al jugador | AUTO | 2s |
| 8 | Bowser Jr | `bowser_jr` | 10000 | Va al Nether, vuelve con blaze rods + pearls | CLICK | 5min |

---

### NPCEntity.java (Clase Base)

```java
public abstract class NPCEntity extends PathfinderMob {
    
    protected static final EntityDataAccessor<Boolean> HIRED = 
        SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> OWNER = 
        SynchedEntityData.defineId(NPCEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    
    private int abilityCooldown = 0;
    
    public NPCEntity(EntityType<? extends NPCEntity> type, Level level) {
        super(type, level);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HIRED, false);
        this.entityData.define(OWNER, Optional.empty());
    }
    
    // Abstract methods
    public abstract int getHireCost();
    public abstract String getNPCId();
    public abstract void performAbility(Player owner);
    public abstract int getAbilityCooldown();
    public abstract boolean isAutoAbility();
    
    // Getters/Setters
    public boolean isHired() { return this.entityData.get(HIRED); }
    public void setHired(boolean hired) { this.entityData.set(HIRED, hired); }
    public Optional<UUID> getOwnerUUID() { return this.entityData.get(OWNER); }
    
    public void setOwner(Player player) {
        this.entityData.set(OWNER, Optional.of(player.getUUID()));
        this.setHired(true);
    }
    
    @Nullable
    public Player getOwnerPlayer() {
        return getOwnerUUID()
            .map(uuid -> this.level().getPlayerByUUID(uuid))
            .orElse(null);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide && isHired()) {
            Player owner = getOwnerPlayer();
            if (owner != null) {
                followOwner(owner);
                
                if (isAutoAbility() && abilityCooldown <= 0) {
                    performAbility(owner);
                    abilityCooldown = getAbilityCooldown();
                }
                
                if (abilityCooldown > 0) abilityCooldown--;
            }
        }
    }
    
    protected void followOwner(Player owner) {
        double distance = this.distanceTo(owner);
        if (distance > 4 && distance < 30) {
            this.getNavigation().moveTo(owner, 1.2);
        } else if (distance >= 30) {
            this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        }
    }
    
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            if (!isHired()) {
                return tryHire(player);
            } else if (!isAutoAbility() && abilityCooldown <= 0) {
                performAbility(player);
                abilityCooldown = getAbilityCooldown();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }
    
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(PlayerDataProvider.PLAYER_DATA).map(data -> {
            if (data.spendCoins(getHireCost())) {
                setOwner(player);
                data.hire(getNPCId());
                spawnQuestionBlock();
                
                this.level().playSound(null, this.blockPosition(), 
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(
                    Component.literal("¡" + this.getName().getString() + " contratado!")
                    .withStyle(ChatFormatting.GREEN), false);
                
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(
                    Component.literal("Necesitas " + getHireCost() + " coins")
                    .withStyle(ChatFormatting.RED), true);
                return InteractionResult.FAIL;
            }
        }).orElse(InteractionResult.FAIL);
    }
    
    protected void spawnQuestionBlock() {
        BlockPos pos = this.blockPosition().above(2);
        Block questionBlock = ForgeRegistries.BLOCKS.getValue(
            new ResourceLocation("super_mario", "question_mark_block"));
        if (questionBlock != null) {
            this.level().setBlock(pos, questionBlock.defaultBlockState(), 3);
        }
    }
}
```

---

### ToadEntity.java

```java
public class ToadEntity extends NPCEntity {
    
    @Override public String getNPCId() { return "toad"; }
    @Override public int getHireCost() { return 10; }
    @Override public int getAbilityCooldown() { return 100; } // 5s
    @Override public boolean isAutoAbility() { return true; }
    
    @Override
    public void performAbility(Player owner) {
        List<Animal> animals = this.level().getEntitiesOfClass(
            Animal.class, 
            this.getBoundingBox().inflate(10),
            a -> a instanceof Cow || a instanceof Pig || a instanceof Sheep || a instanceof Chicken
        );
        
        if (!animals.isEmpty()) {
            Animal target = animals.get(0);
            
            ((ServerLevel)this.level()).sendParticles(
                ParticleTypes.SWEEP_ATTACK,
                target.getX(), target.getY() + 0.5, target.getZ(),
                1, 0, 0, 0, 0
            );
            
            target.kill();
            owner.getInventory().add(new ItemStack(Items.COOKED_BEEF, 2));
            
            this.level().playSound(null, this.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 1.0f, 1.0f);
        }
    }
}
```

---

### YoshiPlaceholderEntity.java

```java
public class YoshiPlaceholderEntity extends NPCEntity {
    
    @Override public String getNPCId() { return "yoshi"; }
    @Override public int getHireCost() { return 50; }
    @Override public int getAbilityCooldown() { return 0; }
    @Override public boolean isAutoAbility() { return false; }
    @Override public void performAbility(Player owner) {}
    
    @Override
    protected InteractionResult tryHire(Player player) {
        return player.getCapability(PlayerDataProvider.PLAYER_DATA).map(data -> {
            if (data.spendCoins(getHireCost())) {
                data.hire(getNPCId());
                
                // Spawn Yoshi REAL del mod super_mario
                EntityType<?> yoshiType = ForgeRegistries.ENTITY_TYPES.getValue(
                    new ResourceLocation("super_mario", "yoshi"));
                if (yoshiType != null) {
                    Entity yoshi = yoshiType.create(this.level());
                    if (yoshi != null) {
                        yoshi.setPos(this.getX(), this.getY(), this.getZ());
                        if (yoshi instanceof TamableAnimal tamable) {
                            tamable.tame(player);
                        }
                        this.level().addFreshEntity(yoshi);
                    }
                }
                
                spawnQuestionBlock();
                this.level().playSound(null, this.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(
                    Component.literal("¡Yoshi contratado! Click derecho para montarlo")
                    .withStyle(ChatFormatting.GREEN), false);
                
                this.discard();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;
        }).orElse(InteractionResult.FAIL);
    }
}
```

---

### LuigiEntity.java

```java
public class LuigiEntity extends NPCEntity {
    
    private boolean isMining = false;
    private int miningTimer = 0;
    
    @Override public String getNPCId() { return "luigi"; }
    @Override public int getHireCost() { return 200; }
    @Override public int getAbilityCooldown() { return 600; }
    @Override public boolean isAutoAbility() { return false; }
    
    @Override
    public void performAbility(Player owner) {
        if (!isMining) {
            isMining = true;
            miningTimer = 300; // 15 segundos
            this.setInvisible(true);
            
            ((ServerLevel)this.level()).sendParticles(
                ParticleTypes.POOF,
                this.getX(), this.getY() + 1, this.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );
            
            owner.displayClientMessage(
                Component.literal("Luigi: ¡Voy a buscar hierro!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC), false);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide && isMining) {
            miningTimer--;
            if (miningTimer <= 0) {
                returnFromMining();
            }
        }
    }
    
    private void returnFromMining() {
        isMining = false;
        this.setInvisible(false);
        
        Player owner = getOwnerPlayer();
        if (owner != null) {
            int amount = 5 + (this.random.nextBoolean() ? 1 : 0);
            owner.getInventory().add(new ItemStack(Items.IRON_INGOT, amount));
            
            ((ServerLevel)this.level()).sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                this.getX(), this.getY() + 1, this.getZ(),
                10, 0.5, 0.5, 0.5, 0.1
            );
            
            owner.displayClientMessage(
                Component.literal("Luigi: ¡Encontré " + amount + " hierro!")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC), false);
        }
    }
}
```

---

### LumaEntity.java

```java
public class LumaEntity extends NPCEntity {
    
    @Override public String getNPCId() { return "luma"; }
    @Override public int getHireCost() { return 2500; }
    @Override public int getAbilityCooldown() { return 600; } // 30s
    @Override public boolean isAutoAbility() { return true; }
    
    @Override
    public void performAbility(Player owner) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        
        serverLevel.sendParticles(
            ParticleTypes.FLAME,
            this.getX(), this.getY() + 0.5, this.getZ(),
            100, 3, 2, 3, 0.05
        );
        
        this.level().playSound(null, this.blockPosition(),
            SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.0f, 0.7f);
        
        List<Monster> monsters = this.level().getEntitiesOfClass(
            Monster.class,
            this.getBoundingBox().inflate(12)
        );
        
        for (Monster monster : monsters) {
            monster.setSecondsOnFire(5);
            monster.hurt(this.damageSources().onFire(), 6.0f);
        }
        
        owner.displayClientMessage(
            Component.literal("✦ Luma irradia energía estelar ✦")
            .withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }
}
```

---

### BowserJrEntity.java

```java
public class BowserJrEntity extends NPCEntity {
    
    private boolean inNether = false;
    private int netherTimer = 0;
    
    @Override public String getNPCId() { return "bowser_jr"; }
    @Override public int getHireCost() { return 10000; }
    @Override public int getAbilityCooldown() { return 6000; } // 5 min
    @Override public boolean isAutoAbility() { return false; }
    
    @Override
    public void performAbility(Player owner) {
        if (!inNether) {
            inNether = true;
            netherTimer = 600; // 30 segundos
            this.setInvisible(true);
            
            ServerLevel serverLevel = (ServerLevel) this.level();
            
            serverLevel.sendParticles(
                ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1, this.getZ(),
                100, 0.5, 1, 0.5, 0.5
            );
            
            this.level().playSound(null, this.blockPosition(),
                SoundEvents.PORTAL_TRAVEL, SoundSource.NEUTRAL, 0.5f, 1.0f);
            
            owner.displayClientMessage(
                Component.literal("Bowser Jr: ¡Voy al Nether! ¡Dame 30 segundos!")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), false);
        }
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide && inNether) {
            netherTimer--;
            if (netherTimer <= 0) {
                returnFromNether();
            }
        }
    }
    
    private void returnFromNether() {
        inNether = false;
        this.setInvisible(false);
        
        Player owner = getOwnerPlayer();
        if (owner != null) {
            ServerLevel serverLevel = (ServerLevel) this.level();
            
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1, this.getZ(),
                50, 0.5, 1, 0.5, 0.3
            );
            
            owner.getInventory().add(new ItemStack(Items.BLAZE_ROD, 12));
            owner.getInventory().add(new ItemStack(Items.ENDER_PEARL, 16));
            
            owner.displayClientMessage(
                Component.literal("Bowser Jr: ¡He vuelto! Toma todo lo que necesitas para el End.")
                .withStyle(ChatFormatting.RED, ChatFormatting.ITALIC), false);
        }
    }
}
```

---

## EVENTOS - RECOLECCIÓN DE ITEMS

```java
@Mod.EventBusSubscriber(modid = MarioGalaxyHelpers.MODID)
public class ItemPickupEvents {
    
    @SubscribeEvent
    public static void onItemPickup(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItem().getItem();
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        
        if (itemId == null) return;
        
        player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
            String id = itemId.toString();
            
            if (id.equals("super_block_world:coin")) {
                data.addCoins(stack.getCount());
                player.displayClientMessage(
                    Component.literal("+" + stack.getCount() + " coins")
                    .withStyle(ChatFormatting.GOLD), true);
            }
            else if (id.startsWith("super_mario:yoshi_egg")) {
                data.addCoins(10 * stack.getCount());
                player.displayClientMessage(
                    Component.literal("+" + (10 * stack.getCount()) + " coins (Yoshi Egg)")
                    .withStyle(ChatFormatting.GOLD), true);
            }
            else if (id.equals("super_mario:star_bit")) {
                data.addCoins(50 * stack.getCount());
                player.displayClientMessage(
                    Component.literal("+" + (50 * stack.getCount()) + " coins (Star Bit)")
                    .withStyle(ChatFormatting.GOLD), true);
            }
            else if (id.equals("super_mario:power_star")) {
                data.addCoins(333 * stack.getCount());
                player.displayClientMessage(
                    Component.literal("⭐ POWER STAR ⭐ +333 coins")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD), false);
            }
        });
    }
    
    @SubscribeEvent
    public static void onEntityKilled(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            player.getCapability(PlayerDataProvider.PLAYER_DATA).ifPresent(data -> {
                if (!data.isHired("toad")) {
                    data.addCoins(1);
                    player.displayClientMessage(
                        Component.literal("+1 coin").withStyle(ChatFormatting.GOLD), true);
                }
            });
        }
    }
}
```

---

## COMANDOS DEBUG

```java
/mariogalaxy coins add <amount>  - Añadir coins
/mariogalaxy spawn <npc>         - Spawnear NPC
/mariogalaxy spawn all           - Spawnear todos
/mariogalaxy reset               - Resetear datos del jugador
```

---

## CHECKPOINTS DE IMPLEMENTACIÓN

### CHECKPOINT 1 - Setup Base
- [ ] Crear proyecto Forge MDK 1.20.1
- [ ] Configurar build.gradle y mods.toml
- [ ] Clase principal MarioGalaxyHelpers.java
- [ ] **COMPILAR:** `./gradlew build` - debe compilar sin errores
- [ ] **VERIFICAR:** El mod carga en Minecraft (ver logs)

### CHECKPOINT 2 - Sistema de Coins
- [ ] PlayerData.java (clase de datos)
- [ ] PlayerDataProvider.java (capability provider)
- [ ] ModCapabilities.java (registro)
- [ ] Eventos para attach capability al jugador
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** Entrar al mundo, no debe crashear

### CHECKPOINT 3 - HUD de Coins
- [ ] CoinHudOverlay.java (renderizar coins en pantalla)
- [ ] Registrar evento de render GUI
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** Ver "🪙 0 coins" en pantalla al entrar

### CHECKPOINT 4 - Comandos Debug
- [ ] ModCommands.java
- [ ] Comando /mariogalaxy coins add <amount>
- [ ] Comando /mariogalaxy coins set <amount>
- [ ] Registrar comandos
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** /mariogalaxy coins add 100 → HUD muestra 100

### CHECKPOINT 5 - NPC Base + Toad
- [ ] NPCEntity.java (clase abstracta base)
- [ ] ToadEntity.java (primer NPC)
- [ ] ModEntities.java (registro de entidades)
- [ ] Spawn egg o comando para spawnear Toad
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** Spawnear Toad, debe aparecer (modelo placeholder OK)

### CHECKPOINT 6 - Sistema de Contratación
- [ ] Lógica de tryHire() en NPCEntity
- [ ] Spawn de question_mark_block al contratar
- [ ] Toad sigue al jugador después de contratar
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** 
  - /mariogalaxy coins add 100
  - Click en Toad → "¡Toad contratado!"
  - Toad te sigue
  - Bloque ? aparece

### CHECKPOINT 7 - Habilidad de Toad
- [ ] performAbility() de Toad (cazar animales)
- [ ] Timer de cooldown funcionando
- [ ] Drop de comida cocinada
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** Toad mata vacas cercanas y da comida

### CHECKPOINT 8 - Resto de NPCs (uno por uno)
Para cada NPC, implementar y verificar antes del siguiente:

- [ ] **PeachEntity** (solo regen, muy simple)
  - Compilar + Verificar regeneración funciona
  
- [ ] **LuigiEntity** (cooldown + invisible + return)
  - Compilar + Verificar desaparece y vuelve con hierro
  
- [ ] **LumaEntity** (AOE fire damage)
  - Compilar + Verificar quema mobs cercanos
  
- [ ] **CaptainToadEntity** (partículas hacia estructura)
  - Compilar + Verificar partículas aparecen
  
- [ ] **RosalinaEntity** (detectar menas)
  - Compilar + Verificar partículas en menas
  
- [ ] **BowserJrEntity** (Nether trip)
  - Compilar + Verificar desaparece y vuelve con items
  
- [ ] **YoshiPlaceholderEntity** (spawn Yoshi del mod)
  - Compilar + Verificar spawnea super_mario:yoshi

### CHECKPOINT 9 - Eventos de Items
- [ ] ItemPickupEvents.java
- [ ] Detectar pickup de coins → añadir al score
- [ ] Detectar pickup de yoshi_egg → +10 coins
- [ ] Detectar pickup de star_bit → +50 coins
- [ ] Detectar pickup de power_star → +333 coins
- [ ] Detectar kill de mob (si Toad no contratado) → +1 coin
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** Recoger items del mod super_mario da coins

### CHECKPOINT 10 - Integración cpmodel_entity
- [ ] Verificar compatibilidad de NPCEntity con cpmodel_entity
- [ ] Configurar tags/NBT necesarios para cargar modelos CPM
- [ ] Testear que los modelos de Mario Galaxy se renderizan
- [ ] **COMPILAR:** `./gradlew build`
- [ ] **VERIFICAR:** NPCs aparecen con modelos de Mario (no como humanoides genéricos)

### CHECKPOINT FINAL - Testing Completo
- [ ] Spawnear todos los NPCs
- [ ] Contratar cada uno en orden
- [ ] Verificar todas las habilidades
- [ ] Verificar bloque ? aparece en cada contratación
- [ ] Verificar seguimiento de todos los NPCs
- [ ] **BUILD FINAL:** `./gradlew build`
- [ ] Jar listo en build/libs/

---

## NOTAS IMPORTANTES

- **Es un MOD Forge, NO datapack**
- **Java 17**
- **El Yoshi montable ya existe en super_mario** - solo spawnear la entidad
- **El question_mark_block ya existe** - solo setBlock
- **Usar Capabilities para datos del jugador**
- **Compilar:** `./gradlew build`

## RENDERIZADO - CPMODEL_ENTITY

**NO usar modelos de Villager ni crear renderers custom vanilla.**

Los NPCs usan el mod `cpmodel_entity` para renderizar modelos de CustomPlayerModels (CPM).

- Las entidades NPC deben ser compatibles con cpmodel_entity
- Los modelos CPM de los personajes de Mario Galaxy ya existen
- cpmodel_entity se encarga del renderizado, solo hay que configurar las entidades correctamente
- Revisar el código/documentación de cpmodel_entity para:
  - Qué clase base usar para las entidades
  - Qué tags o NBT se necesitan para asignar un modelo CPM
  - Cómo asociar un modelo específico a cada tipo de NPC

## REGLAS DE DESARROLLO

1. **Compilar después de cada checkpoint** - No avanzar si hay errores
2. **Verificar en juego** - Cada checkpoint tiene una verificación manual
3. **Un NPC a la vez** - No implementar todos juntos
4. **Commit después de cada checkpoint** - Poder hacer rollback si algo falla
5. **Si algo no compila, arreglarlo antes de continuar**
