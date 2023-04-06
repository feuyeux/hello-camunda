from camunda.external_task.external_task import ExternalTask, TaskResult
from camunda.external_task.external_task_worker import ExternalTaskWorker

default_config = {

}


def handle_task(task: ExternalTask) -> TaskResult:
    # ..
    return task.complete()


if __name__ == '__main__':
    worker = ExternalTaskWorker(
        worker_id="python-client",
        base_url="http://localhost:8080/engine-rest",
        config=default_config).subscribe("", handle_task())
