/**
*  This file is part of Healthcare Tasks: Human task management in healthcare contexts.
*  Copyright (C) 2025  Universidad de Sevilla/Departamento de Ingeniería Telemática
*
*  Healthcare Tasks is free software: you can redistribute it and/or
*  modify it under the terms of the GNU General Public License as published
*  by the Free Software Foundation, either version 3 of the License, or (at
*  your option) any later version.
*
*  Healthcare Tasks is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
*  Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with Healthcare Tasks. If not, see <https://www.gnu.org/licenses/>.
**/
/*
  Autor: Juan Manuel Ostos Rabadán
*/

let currentSortColumn = '';
let sortDirection = 'asc';
// esta función ordena las filas de la tabla según la columna seleccionada y la dirección del orden
function sortByColumn(column) {
    const rows = Array.from(document.querySelectorAll('tbody tr')); //Se recopilan todas las filas en un array para poder ordenarlas
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
        if (!isNaN(Date.parse(aText)) && !isNaN(Date.parse(bText))) { //Si el contenido de ambas celdas puede interpretarse como una fecha válida se ordenan como fechas.
            return sortDirection === 'asc'
                ? new Date(aText) - new Date(bText)
                : new Date(bText) - new Date(aText);
        }
        //Si no, se ordenan como texto
        return sortDirection === 'asc'
            ? aText.localeCompare(bText)
            : bText.localeCompare(aText);
    });

    tableBody.innerHTML = ''; //Se borra el contenido original de la tabla.
    rows.forEach(row => tableBody.appendChild(row)); //Se vuelve a insertar cada fila en el nuevo orden.

    headerCells.forEach(th => {
        th.querySelector('.sort-indicator').textContent = '';
    });

    const activeHeader = document.querySelector(`th[data-column="${column}"]`);
    activeHeader.querySelector('.sort-indicator').textContent = sortDirection === 'asc' ? '▲' : '▼';
}