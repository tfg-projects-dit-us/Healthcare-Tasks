function filterTable() {
    const column = document.getElementById('filterColumn').value;
    const filterInput = document.getElementById('textFilterInput').value.toLowerCase();
    const priorityValue = document.getElementById('prioritySelect').value;
    const statusValue = document.getElementById('statusSelect').value;
    const startDate = document.getElementById('startDate').value;
    const endDate = document.getElementById('endDate').value;
    const rows = document.querySelectorAll('tbody tr');

    rows.forEach(row => {
        if (column === '') {
            row.style.display = '';
            return;
        }
        const cell = row.querySelector(`td[data-column="${column}"]`);
        const text = cell ? cell.textContent.toLowerCase() : '';
        let showRow = true;

        if (column === 'createdOn' || column === 'expirationTime') {
            const dateTimeText = cell ? cell.textContent.replaceAll('/', '-') : '';
            const cellDateTime = new Date(dateTimeText);
            if (startDate && new Date(startDate) > cellDateTime) showRow = false;
            if (endDate && new Date(endDate) < cellDateTime) showRow = false;
        } else if (column === 'priority') {
            if (priorityValue && text !== priorityValue.toLowerCase()) showRow = false;
        } else if (column === 'status') {
            if (statusValue && text !== statusValue.toLowerCase()) showRow = false;
        } else {
            showRow = text.includes(filterInput);
        }

        row.style.display = showRow ? '' : 'none';
    });
}

function toggleDateInputs() {
    //reseteamos valores de inputs
    document.getElementById('textFilterInput').value='';
    document.getElementById('prioritySelect').value='';
    document.getElementById('statusSelect').value='';
    document.getElementById('startDate').value='';
    document.getElementById('endDate').value='';

    const column = document.getElementById('filterColumn').value;
    const showDate = column === 'createdOn' || column === 'expirationTime';
    const isPriority = column === 'priority';
    const showTextInput = column === 'name' || column === 'subject';
    const isStatus = column === 'status';
    
    //mostramos el input de la columna seleccionada
    document.getElementById('dateRangeInputs').style.display = showDate ? 'inline-block' : 'none';
    document.getElementById('textFilterInput').style.display = showTextInput ? 'inline-block' : 'none';
    document.getElementById('prioritySelect').style.display = isPriority ? 'inline-block' : 'none';
    document.getElementById('statusSelect').style.display = isStatus ? 'inline-block' : 'none';
    filterTable();
}
