# NoFly

A fly detection plugin for Paper 1.21.x. It combines synchronous movement analysis with PacketEvents packet sanity checks.

## Install

1. Install [PacketEvents](https://github.com/retrooper/packetevents) on your server.
2. Drop `NoFly-1.0.0.jar` into `plugins/`.
3. Start the server once to generate `config.yml`.
4. Tune `leniency` upward for unusually noisy movement environments.

## Checks

The movement monitor runs once per server tick and tracks the player's last accepted position, vertical motion, air time, sustained hovering, and sustained ascent.

The vertical check compares measured vertical displacement with the previous tick's expected gravity step. It also detects prolonged near-zero vertical motion and prolonged upward movement while airborne.

The horizontal check looks for sustained horizontal movement while vertical movement remains close to zero. It is intentionally buffered so a single unusual tick does not cause a setback.

PacketEvents rejects non-finite or unreasonable movement coordinates. Server-side glide events validate Elytra start attempts.

Creative, spectator, active flight, Elytra gliding, riptiding, vehicles, fluids, climbing, levitation, slow falling, powder snow, recent damage, recent velocity changes, potion changes, teleports, and players with `nofly.bypass` are exempt from movement checks.

## Commands

| command | permission | description |
| --- | --- | --- |
| `/nofly reload` | `nofly.admin` | reload the configuration |

## Permissions

| permission | default | description |
| --- | --- | --- |
| `nofly.bypass` | op | skip movement checks |
| `nofly.admin` | op | use `/nofly` |
| `nofly.alerts` | op | receive flag alerts |

## Build

```text
gradle build
```

The GitHub Actions workflow installs Gradle 8.10.2 automatically.

## License

MIT
