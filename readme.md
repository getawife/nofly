# NoFly

A fly detection plugin for Paper 1.21.x. It combines synchronous movement analysis with PacketEvents packet sanity checks.

## Install

1. Install [PacketEvents](https://github.com/retrooper/packetevents) on your server.
2. Drop `NoFly-1.0.0.jar` into `plugins/`.
3. Start the server once to generate `config.yml`.
4. Tune `leniency` upward for unusually noisy movement environments.

## Commands

| command         | permission    | description              |
| --------------- | ------------- | ------------------------ |
| `/nofly reload` | `nofly.admin` | reload the configuration |

## Permissions

| permission     | default | description          |
| -------------- | ------- | -------------------- |
| `nofly.bypass` | op      | skip movement checks |
| `nofly.admin`  | op      | use `/nofly`         |
| `nofly.alerts` | op      | receive flag alerts  |

## Build

```text
gradle build
```

The GitHub Actions workflow installs Gradle 8.10.2 automatically.

## License

MIT
