# Redefined Glowing Entities
![Release](https://jitpack.io/v/MsMaciek123/RedefinedGlowingEntities.svg)

**Redefined Glowing Entities** is an API for developers to make entities glowing. \
Supported Minecraft versions: 1.17+ \
Dependencies: PacketEvents \
Since it uses teams to set glowing color, you can also set nickname visibility and collisions.

Example usage (this is your main class implementing listener):
```java
public RedefinedGlowingEntitiesAPI geAPI;

@Override
public void onEnable() {
	geAPI = new RedefinedGlowingEntitiesAPI(this);
	getServer().getPluginManager().registerEvents(this, this);
	// rest of your code
}

@EventHandler
public void onEntityInteractEvent(PlayerInteractAtEntityEvent e) {
	if(!e.getHand().equals(EquipmentSlot.HAND))
		return;

	Entity rightClickedEntity = e.getRightClicked();
	Player player = e.getPlayer();

	geAPI.setGlowing(player, rightClickedEntity, NamedTextColor.GREEN);
	geAPI.setNametagVisiblity(player, rightClickedEntity, GlowTeamNametagVisibility.NEVER);
	geAPI.setCollisionRule(player, rightClickedEntity, GlowTeamCollisionRule.NEVER);
}
```

Replace VERSION with current version. \
Gradle:
```gradle
repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
}
   
dependencies {
    implementation 'com.github.MsMaciek123:RedefinedGlowingEntities:VERSION'
}
```

Maven:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.MsMaciek123</groupId>
    <artifactId>RedefinedGlowingEntities</artifactId>
    <version>VERSION</version>
</dependency>
```