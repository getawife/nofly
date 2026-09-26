# NoFly

A fly-specific anti-cheat for Paper 1.21.x. Works by simulating movement server side and validating the client side packets.

## Install

1. Install [PacketEvents](https://github.com/retrooper/packetevents) on your server.
2. Drop `NoFly-1.0.0.jar` into `plugins/`.
3. Start the server once to generate `config.yml`.
4. Tune `leniency` upward if you have high-ping players (default `1.15`).

## Commands

| command         | permission    | description       |
| --------------- | ------------- | ----------------- |
| `/nofly reload` | `nofly.admin` | reload the config |

## Permissions

| permission     | default | description         |
| -------------- | ------- | ------------------- |
| `nofly.bypass` | op      | skip all fly checks |
| `nofly.admin`  | op      | use `/nofly`        |

## Config

```yaml
leniency: 1.15

vertical:
  enabled: true
  tolerance: 0.08
  buffer: 2.0
  decay: 0.5

horizontal:
  enabled: true
  min_ticks: 10
  max_vertical: 0.01
  min_horizontal: 0.1

glide:
  enabled: true
  speed_tolerance: 0.12
  buffer: 2.5
  decay: 0.5

timer:
  enabled: true
  min_ms: 40
  buffer: 20

effects:
  max_tracked_ms: 600000

punish:
  alert: true
  log_file: flags.log
```

## Build

```bash
./gradlew build
```

If the wrapper jar is missing, run `gradle wrapper` once first.

Output: `build/libs/NoFly-1.0.0.jar`.

## License

MIT
