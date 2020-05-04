
function fetchServices() {
    let servicesRequest = new Request('/service');
    fetch(servicesRequest)
        .then(function(response) { return response.json(); })
        .then(renderServices)
}

function renderServices(serviceList) {
    const listContainer = document.querySelector('#service-list');
    const listBody = listContainer.getElementsByTagName('tbody')[0];
    serviceList.forEach(service => {
        const newRow = listBody.insertRow(listBody.rows.length);

        const newServiceName = newRow.insertCell(0);
        newServiceName.appendChild(document.createTextNode(service.name));

        const newUrlName = newRow.insertCell(1);
        newUrlName.appendChild(document.createTextNode(service.url));

        const createdDate = newRow.insertCell(2);
        createdDate.appendChild(document.createTextNode(service.createdAt));

        const status = newRow.insertCell(3);
        status.appendChild(document.createTextNode(service.status));

        const actions = newRow.insertCell(4);

        const editBtn = document.createElement('a');
        const iconEdit = document.createElement('i');
        editBtn.setAttribute('class', 'edit');
        editBtn.addEventListener('click', () => {
            editService(service.name, service.url)
        });
        editBtn.setAttribute('type', 'button');
        editBtn.setAttribute('title', 'edit');
        editBtn.setAttribute('data-toggle', 'tooltip')
        iconEdit.setAttribute('class', 'material-icons');
        iconEdit.innerHTML = '&#xE254;';
        editBtn.appendChild(iconEdit);

        const deleteBtn = document.createElement('a');
        const icon = document.createElement('i');
        deleteBtn.setAttribute('class', 'delete');
        deleteBtn.addEventListener('click', () => {
            deleteService(service.id)
        });
        deleteBtn.setAttribute('type', 'button');
        deleteBtn.setAttribute('title', 'delete');
        deleteBtn.setAttribute('data-toggle', 'tooltip')
        icon.setAttribute('class', 'material-icons');
        icon.innerHTML = '&#xE872;';
        deleteBtn.appendChild(icon);

        actions.appendChild(editBtn);
        actions.appendChild(deleteBtn);

    });
}

document.addEventListener('DOMContentLoaded', () => {
    fetchServices();
});

function editService(name, url) {
    document.getElementById('url').value = url;
    document.getElementById('name').value = name;
}

const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {
    evt.preventDefault();

    let url = document.querySelector('#url').value;
    let name = document.querySelector('#name').value;

    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({url, name}),
}).then(response => location.reload());
}

function deleteService(id) {
    fetch('/service/' + id, {
        method: 'delete',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
    }).then(response => location.reload());
}