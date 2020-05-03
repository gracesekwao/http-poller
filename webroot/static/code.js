const listContainer = document.querySelector('#service-list');
const listBody = listContainer.getElementsByTagName('tbody')[0];
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
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

  });
});


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