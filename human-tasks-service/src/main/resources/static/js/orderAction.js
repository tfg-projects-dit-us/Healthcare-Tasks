let currentSortColumn = '';
let sortDirection = 'asc';
function sortByColumn(column) {
    const rows = Array.from(document.querySelectorAll('tbody tr'));
    const tableBody = document.querySelector('tbody');
    const headerCells = document.querySelectorAll('th.sortable');

    if (currentSortColumn === column) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        currentSortColumn = column;
        sortDirection = 'asc';
    }

    rows.sort((a, b) => {
        const aText = a.querySelector(`td[data-column="${column}"]`).textContent.trim();
        const bText = b.querySelector(`td[data-column="${column}"]`).textContent.trim();
        if (!isNaN(Date.parse(aText)) && !isNaN(Date.parse(bText))) {
            return sortDirection === 'asc'
                ? new Date(aText) - new Date(bText)
                : new Date(bText) - new Date(aText);
        }
        return sortDirection === 'asc'
            ? aText.localeCompare(bText)
            : bText.localeCompare(aText);
    });

    tableBody.innerHTML = '';
    rows.forEach(row => tableBody.appendChild(row));

    headerCells.forEach(th => {
        th.querySelector('.sort-indicator').textContent = '';
    });

    const activeHeader = document.querySelector(`th[data-column="${column}"]`);
    activeHeader.querySelector('.sort-indicator').textContent = sortDirection === 'asc' ? '▲' : '▼';
}