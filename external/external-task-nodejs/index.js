import {Client, logger, Variables} from "camunda-external-task-client-js"

const config = {
    baseUrl: 'http://localhost:8080/engine-rest',
    use: logger,
    asyncResponseTimeout: 10_000
}

const client = new Client(config)

client.subscribe('pay', {processDefinitionKey: "Process_shopping"},
    async function ({task, taskService}) {
        const size = task.variables.get('size')
        const count = task.variables.get('count')
        console.log(`下单尺寸:${size} 数量:${count}`)
        const processVariables = new Variables().set("toWhere", "shanghai China")
        try {
            await taskService.complete(task, processVariables)
            console.log("Task completed")
        } catch (e) {
            console.error(`Task failed,${e}`)
        }
    })