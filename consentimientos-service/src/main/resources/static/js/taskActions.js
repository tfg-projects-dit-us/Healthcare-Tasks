function claimTask(taskId, containerId) {
    window.location.href = `/tasks/claim?taskId=${taskId}&containerId=${containerId}`;
}

function startTask(taskId, actualOwner, containerId, processInstanceId) {
    window.location.href = `/tasks/start?taskId=${taskId}&actualOwner=${actualOwner}&containerId=${containerId}&processInstanceId=${processInstanceId}`;
}

function continueTask(taskId, actualOwner, containerId, processInstanceId) {
    window.location.href = `/tasks/continue?taskId=${taskId}&actualOwner=${actualOwner}&containerId=${containerId}&processInstanceId=${processInstanceId}`;
}

function rejectTask(taskId, actualOwner, containerId) {
    window.location.href = `/tasks/reject?taskId=${taskId}&actualOwner=${actualOwner}&containerId=${containerId}`;
}
