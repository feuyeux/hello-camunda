from camunda.external_task.external_task import ExternalTask, TaskResult
from camunda.external_task.external_task_worker import ExternalTaskWorker

# configuration for the Client
default_config = {
    "maxTasks": 1,
    "lockDuration": 10000,
    "asyncResponseTimeout": 5000,
    "retries": 3,
    "retryTimeout": 5000,
    "sleepSeconds": 30
}


def handle_task(task: ExternalTask) -> TaskResult:
    x4 = task.get_variable("x4")
    suffix = task.get_task_id()
    x5 = str(x4) + "_" + suffix
    return task.complete({"x5": x5})


if __name__ == '__main__':
    worker = ExternalTaskWorker(
        worker_id="python-client",
        base_url="http://localhost:8080/engine-rest",
        config=default_config).subscribe("ask_suffix", handle_task)
