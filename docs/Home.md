Welcome to GitTor-Web wiki!

## Required API:

> **💡 Note:** These are not all the endpoints supported by the api, just the ones necessary for the [GitTor CLI](https://github.com/GitTor-ISU/GitTor-Cli) to function.
> Other GitTor web hostings may support other endpoints; however, these are the minimum needed across all hostings.

✅ _Complete_ <br>
📝 _In progress_ <br>
❌ _Not implemented_ <br>

### [Login](https://gittor-isu.github.io/GitTor-Web/openapi.html#tag/Authentication/operation/login) ✅

Given a username and password, must return JWT token.

### Upload Repository ❌

Given a repository torrent file, must create a new repository.

### Update Repository ❌

Given a repository torrent file and repository unique identifier, must update the existing repository with the new torrent file.

### Get Repository ❌

Given repository unique identifier, must must return repository torrent file.
