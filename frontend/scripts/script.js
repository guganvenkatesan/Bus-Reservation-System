document.addEventListener('DOMContentLoaded', () => {
    const API_BASE_URL = 'http://localhost:8082/api';

    // Sections
    const bookingOptions = document.getElementById('bookingOptions');
    const manualBookingForm = document.getElementById('manualBookingForm');
    const preloadedBookingForm = document.getElementById('preloadedBookingForm');
    const availableBuses = document.getElementById('availableBuses');
    const seatSelection = document.getElementById('seatSelection');
    const bookingConfirmation = document.getElementById('bookingConfirmation');
    const cancellationForm = document.getElementById('cancellationForm');
    const cancellationConfirmation = document.getElementById('cancellationConfirmation');
    const adminPanel = document.getElementById('adminPanel');
    const userProfile = document.getElementById('userProfile');
    const sections = [bookingOptions, manualBookingForm, preloadedBookingForm, availableBuses, seatSelection, bookingConfirmation, cancellationForm, cancellationConfirmation, adminPanel, userProfile];

    // Buttons
    const manualBookingBtn = document.getElementById('manualBookingBtn');
    const preloadedBookingBtn = document.getElementById('preloadedBookingBtn');
    const cancelBookingBtn = document.getElementById('cancelBookingBtn');
    const adminPanelBtn = document.getElementById('adminPanelBtn');
    const userProfileBtn = document.getElementById('userProfileBtn');
    const manualBackBtn = document.getElementById('manualBackBtn');
    const preloadedBackBtn = document.getElementById('preloadedBackBtn');
    const busesBackBtn = document.getElementById('busesBackBtn');
    const seatSelectionBackBtn = document.getElementById('seatSelectionBackBtn');
    const confirmBookingBtn = document.getElementById('confirmBookingBtn');
    const newBookingBtn = document.getElementById('newBookingBtn');
    const cancelBackBtn = document.getElementById('cancelBackBtn');
    const newCancellationBtn = document.getElementById('newCancellationBtn');
    const adminBackBtn = document.getElementById('adminBackBtn');
    const refreshBookingsBtn = document.getElementById('refreshBookingsBtn');
    const userProfileBackBtn = document.getElementById('userProfileBackBtn');

    // Forms
    const manualForm = document.getElementById('manualForm');
    const preloadedForm = document.getElementById('preloadedForm');
    const cancelForm = document.getElementById('cancelForm');
    const userProfileForm = document.getElementById('userProfileForm');

    // Display areas
    const busesList = document.getElementById('busesList');
    const seatMap = document.getElementById('seatMap');
    const confirmationDetails = document.getElementById('confirmationDetails');
    const cancellationDetails = document.getElementById('cancellationDetails');
    const adminBookingsList = document.getElementById('adminBookingsList');
    const userProfileDetails = document.getElementById('userProfileDetails');
    const userBookingsList = document.getElementById('userBookingsList');

    // State
    let currentBookingData = {};

    // --- Event Listeners ---

    manualBookingBtn.addEventListener('click', () => showSection(manualBookingForm));
    preloadedBookingBtn.addEventListener('click', () => showSection(preloadedBookingForm));
    cancelBookingBtn.addEventListener('click', () => showSection(cancellationForm));
    adminPanelBtn.addEventListener('click', async () => {
        await fetchAndDisplayBookings();
        showSection(adminPanel);
    });
    userProfileBtn.addEventListener('click', () => showSection(userProfile));
    manualBackBtn.addEventListener('click', () => showSection(bookingOptions));
    preloadedBackBtn.addEventListener('click', () => showSection(bookingOptions));
    busesBackBtn.addEventListener('click', () => showSection(preloadedBookingForm));
    seatSelectionBackBtn.addEventListener('click', () => showSection(availableBuses));
    cancelBackBtn.addEventListener('click', () => showSection(bookingOptions));
    adminBackBtn.addEventListener('click', () => showSection(bookingOptions));
    userProfileBackBtn.addEventListener('click', () => {
        userProfileDetails.classList.add('hidden');
        showSection(bookingOptions);
    });
    refreshBookingsBtn.addEventListener('click', fetchAndDisplayBookings);
    newBookingBtn.addEventListener('click', () => {
        resetForms();
        showSection(bookingOptions);
    });
    newCancellationBtn.addEventListener('click', () => {
        resetForms();
        showSection(bookingOptions);
    });

    confirmBookingBtn.addEventListener('click', async () => {
        const selectedSeats = document.querySelectorAll('.seat.selected');
        if (selectedSeats.length === 0) {
            alert('Please select at least one seat.');
            return;
        }
        if (selectedSeats.length > currentBookingData.passengers) {
            alert(`You can only select up to ${currentBookingData.passengers} seats.`);
            return;
        }
        
        const seatNumbers = Array.from(selectedSeats).map(seat => seat.dataset.seatNumber);
        await bookPreloadedBus(currentBookingData.busId, currentBookingData, seatNumbers);
    });

    manualForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = {
            name: document.getElementById('manualName').value,
            source: document.getElementById('manualSource').value,
            destination: document.getElementById('manualDestination').value,
            date: document.getElementById('manualDate').value,
            passengers: parseInt(document.getElementById('manualPassengers').value),
            farePerPassenger: parseInt(document.getElementById('manualFare').value)
        };
        await handleManualBooking(formData);
    });

    preloadedForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = {
            name: document.getElementById('preloadedName').value,
            source: document.getElementById('preloadedSource').value,
            destination: document.getElementById('preloadedDestination').value,
            date: document.getElementById('preloadedDate').value,
            passengers: parseInt(document.getElementById('preloadedPassengers').value)
        };
        await handlePreloadedSearch(formData);
    });

    cancelForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const bookingId = parseInt(document.getElementById('bookingId').value);
        await handleCancellation(bookingId);
    });

    userProfileForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const username = document.getElementById('username').value;
        await fetchAndDisplayUserProfile(username);
    });

    // --- Helper Functions ---

    function showSection(sectionToShow) {
        sections.forEach(section => {
            section.classList.add('hidden');
        });
        if (sectionToShow) {
            sectionToShow.classList.remove('hidden');
        }
    }

    function resetForms() {
        manualForm.reset();
        preloadedForm.reset();
        cancelForm.reset();
        userProfileForm.reset();
        currentBookingData = {};
    }
    
    // Initial state
    showSection(bookingOptions);

    async function handleManualBooking(formData) {
        try {
            const response = await fetch(`${API_BASE_URL}/manual-booking`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const result = await response.json();
                confirmationDetails.innerHTML = `
                    <p><strong>Booking ID:</strong> ${result.bookingId}</p>
                    <p><strong>Name:</strong> ${formData.name}</p>
                    <p><strong>Route:</strong> ${formData.source} to ${formData.destination}</p>
                    <p><strong>Date:</strong> ${formData.date}</p>
                    <p><strong>Passengers:</strong> ${formData.passengers}</p>
                    <p><strong>Total Fare:</strong> ₹${result.totalFare}</p>
                    <p><strong>Status:</strong> ${result.message}</p>
                `;
                showSection(bookingConfirmation);
            } else {
                const error = await response.json();
                alert(error.error || 'Booking failed');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    async function handlePreloadedSearch(formData) {
        try {
            const response = await fetch(`${API_BASE_URL}/search-buses`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const buses = await response.json();
                displayAvailableBuses(buses, formData);
            } else {
                const error = await response.json();
                alert(error.error || 'Failed to fetch buses');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    function displayAvailableBuses(buses, formData) {
        busesList.innerHTML = '';
        if (buses.length === 0) {
            busesList.innerHTML = '<p>No buses available for the selected route and date.</p>';
        } else {
            const table = document.createElement('table');
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Bus Name</th>
                        <th>Departure</th>
                        <th>Arrival</th>
                        <th>Fare per Seat</th>
                        <th>Seats Available</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    ${buses.map(bus => `
                        <tr>
                            <td>${bus.name}</td>
                            <td>${bus.departure}</td>
                            <td>${bus.arrival}</td>
                            <td>₹${bus.fare_per_seat}</td>
                            <td>${bus.seats_available}</td>
                            <td><button class="book-btn" data-bus-id="${bus.bus_id}">Book</button></td>
                        </tr>
                    `).join('')}
                </tbody>
            `;
            table.querySelectorAll('.book-btn').forEach(button => {
                button.addEventListener('click', (e) => {
                    const busId = e.target.dataset.busId;
                    const bus = buses.find(b => b.bus_id == busId);
                    currentBookingData = { ...formData, busId: parseInt(busId), fare_per_seat: bus.fare_per_seat };
                    renderSeatMap(bus.seats_available, formData.passengers);
                    showSection(seatSelection);
                });
            });
            busesList.appendChild(table);
        }
        showSection(availableBuses);
    }

    function renderSeatMap(totalSeats, passengersToBook) {
        seatMap.innerHTML = '';
        const maxSeatsPerRow = 4;
        let seatNumber = 1;

        for (let i = 0; i < totalSeats; i++) {
            const seat = document.createElement('div');
            seat.classList.add('seat');
            seat.dataset.seatNumber = seatNumber++;
            seat.textContent = seat.dataset.seatNumber;
            
            seat.addEventListener('click', () => {
                const selectedSeats = document.querySelectorAll('.seat.selected').length;
                if (seat.classList.contains('selected')) {
                    seat.classList.remove('selected');
                } else if (selectedSeats < passengersToBook) {
                    seat.classList.add('selected');
                } else {
                    alert(`You can only select up to ${passengersToBook} seats.`);
                }
            });

            seatMap.appendChild(seat);
        }
    }

    async function bookPreloadedBus(busId, formData, seatNumbers) {
        try {
            const bookingData = {
                ...formData,
                busId: parseInt(busId),
                seats: seatNumbers.length,
                passengerName: formData.name // Assuming the main booker's name for all seats
            };
            
            // Use the seat-booking endpoint
            const response = await fetch(`${API_BASE_URL}/book-seat`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookingData)
            });

            if (response.ok) {
                const result = await response.json();
                confirmationDetails.innerHTML = `
                    <p><strong>Booking ID:</strong> ${result.bookingId}</p>
                    <p><strong>Name:</strong> ${formData.name}</p>
                    <p><strong>Route:</strong> ${formData.source} to ${formData.destination}</p>
                    <p><strong>Date:</strong> ${formData.date}</p>
                    <p><strong>Passengers:</strong> ${seatNumbers.length}</p>
                    <p><strong>Seats:</strong> ${seatNumbers.join(', ')}</p>
                    <p><strong>Bus ID:</strong> ${busId}</p>
                    <p><strong>Total Fare:</strong> ₹${result.totalFare}</p>
                    <p><strong>Status:</strong> ${result.message}</p>
                `;
                showSection(bookingConfirmation);
            } else {
                const error = await response.json();
                alert(error.error || 'Booking failed');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    async function handleCancellation(bookingId) {
        try {
            const response = await fetch(`${API_BASE_URL}/cancel-booking`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ bookingId })
            });

            if (response.ok) {
                const result = await response.json();
                cancellationDetails.innerHTML = `<p>${result.message}</p>`;
                showSection(cancellationConfirmation);
            } else {
                const error = await response.json();
                alert(error.error || 'Cancellation failed');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    async function fetchAndDisplayBookings() {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/bookings`, {
                method: 'POST', // Assuming POST as per BaseHandler, though GET might be more appropriate
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({}) // Empty JSON object
            });

            if (response.ok) {
                const bookings = await response.json();
                displayAdminBookings(bookings);
            } else {
                const error = await response.json();
                alert(error.error || 'Failed to fetch bookings');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    function displayAdminBookings(bookings) {
        adminBookingsList.innerHTML = '';
        if (bookings.length === 0) {
            adminBookingsList.innerHTML = '<p>No bookings found.</p>';
        } else {
            const table = document.createElement('table');
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Booking ID</th>
                        <th>Name</th>
                        <th>Route</th>
                        <th>Date</th>
                        <th>Passengers</th>
                        <th>Total Fare</th>
                        <th>Bus ID</th>
                    </tr>
                </thead>
                <tbody>
                    ${bookings.map(booking => `
                        <tr>
                            <td>${booking.booking_id}</td>
                            <td>${booking.name}</td>
                            <td>${booking.source} to ${booking.destination}</td>
                            <td>${booking.date_of_journey}</td>
                            <td>${booking.passengers}</td>
                            <td>₹${booking.total_fare}</td>
                            <td>${booking.bus_id || 'N/A'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            `;
            adminBookingsList.appendChild(table);
        }
    }

    async function fetchAndDisplayUserProfile(username) {
        try {
            const response = await fetch(`${API_BASE_URL}/user/profile`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username })
            });

            if (response.ok) {
                const user = await response.json();
                
                const bookingsResponse = await fetch(`${API_BASE_URL}/user-bookings`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username })
                });

                if (bookingsResponse.ok) {
                    const userBookings = await bookingsResponse.json();
                    userProfileDetails.classList.remove('hidden');
                    displayUserBookings(user, userBookings);
                } else {
                    alert('Could not fetch user bookings.');
                }
            } else {
                const error = await response.json();
                alert(error.error || 'Failed to fetch user profile');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('An error occurred. Please try again.');
        }
    }

    function displayUserBookings(user, bookings) {
        userBookingsList.innerHTML = '';
        if (bookings.length === 0) {
            userBookingsList.innerHTML = `<p>No bookings found for ${user.full_name}.</p>`;
        } else {
            const table = document.createElement('table');
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Booking ID</th>
                        <th>Route</th>
                        <th>Date</th>
                        <th>Passengers</th>
                        <th>Total Fare</th>
                        <th>Bus ID</th>
                    </tr>
                </thead>
                <tbody>
                    ${bookings.map(booking => `
                        <tr>
                            <td>${booking.booking_id}</td>
                            <td>${booking.source} to ${booking.destination}</td>
                            <td>${booking.date_of_journey}</td>
                            <td>${booking.passengers}</td>
                            <td>₹${booking.total_fare}</td>
                            <td>${booking.bus_id || 'N/A'}</td>
                        </tr>
                    `).join('')}
                </tbody>
            `;
            userBookingsList.appendChild(table);
        }
    }
});
