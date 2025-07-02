# Frontend Integration Guide - Trip Planning System

## Table of Contents
1. [API Endpoint Integration](#api-endpoint-integration)
2. [Frontend Architecture](#frontend-architecture)
3. [UI Components Structure](#ui-components-structure)
4. [API Service Layer](#api-service-layer)
5. [State Management](#state-management)
6. [User Interface Examples](#user-interface-examples)
7. [Category Management](#category-management)
8. [Multi-City Trip Flow](#multi-city-trip-flow)

## API Endpoint Integration

### Base Configuration
```javascript
// config/api.js
const API_BASE_URL = 'http://localhost:8080';
const TRIP_API_BASE = `${API_BASE_URL}/trip`;

export const API_ENDPOINTS = {
  // Trip Management
  CREATE_TRIP: `${TRIP_API_BASE}/initiate`,
  GET_MY_TRIPS: `${TRIP_API_BASE}/my-trips`,
  GET_TRIP_SUMMARY: (tripId) => `${TRIP_API_BASE}/${tripId}/summary`,
  
  // Location & Search
  SEARCH_LOCATIONS: `${TRIP_API_BASE}/search-locations`,
  ADD_PLACE: (tripId) => `${TRIP_API_BASE}/${tripId}/add-place`,
  ADD_PLACE_TO_DAY: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}/add-place`,
  
  // AI Features
  GET_SUGGESTIONS: (tripId) => `${TRIP_API_BASE}/${tripId}/suggestions`,
  GET_CONTEXTUAL_SUGGESTIONS: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}/contextual-suggestions`,
  GET_NEARBY_SUGGESTIONS: (tripId) => `${TRIP_API_BASE}/${tripId}/nearby-suggestions`,
  
  // Day Planning
  GET_DAY_PLAN: (tripId, day) => `${TRIP_API_BASE}/${tripId}/day/${day}`,
  OPTIMIZE_ORDER: (tripId) => `${TRIP_API_BASE}/${tripId}/optimize-order`,
  
  // Map & Data
  GET_MAP_DATA: (tripId) => `${TRIP_API_BASE}/${tripId}/map-data`
};
```

## Frontend Architecture

### Technology Stack Recommendation
```json
{
  "framework": "React.js / Vue.js / Next.js",
  "stateManagement": "Redux Toolkit / Zustand / Pinia",
  "styling": "Tailwind CSS / Material-UI / Ant Design",
  "maps": "Google Maps API / Leaflet",
  "http": "Axios / Fetch API",
  "forms": "Formik / React Hook Form",
  "datePicker": "react-datepicker / vuetify-datepicker",
  "notifications": "react-toastify / vue-toastification"
}
```

### Project Structure
```
src/
├── components/
│   ├── TripCreation/
│   │   ├── TripWizard.jsx
│   │   ├── CitySelector.jsx
│   │   ├── CategorySelector.jsx
│   │   └── DateRangePicker.jsx
│   ├── TripPlanning/
│   │   ├── TripDashboard.jsx
│   │   ├── LocationSearch.jsx
│   │   ├── PlaceCard.jsx
│   │   └── SuggestionPanel.jsx
│   ├── DayPlanning/
│   │   ├── DayPlanView.jsx
│   │   ├── TimelineView.jsx
│   │   └── PlaceTimeline.jsx
│   ├── Maps/
│   │   ├── TripMap.jsx
│   │   └── RouteVisualization.jsx
│   └── Common/
│       ├── LoadingSpinner.jsx
│       ├── ErrorBoundary.jsx
│       └── CategoryChip.jsx
├── services/
│   ├── tripService.js
│   ├── locationService.js
│   └── apiClient.js
├── store/
│   ├── tripSlice.js
│   ├── locationSlice.js
│   └── store.js
├── pages/
│   ├── TripCreation.jsx
│   ├── TripPlanning.jsx
│   ├── DayPlanning.jsx
│   └── TripSummary.jsx
└── utils/
    ├── dateHelpers.js
    ├── categoryHelpers.js
    └── mapHelpers.js
```

## API Service Layer

### Trip Service Implementation
```javascript
// services/tripService.js
import axios from 'axios';
import { API_ENDPOINTS } from '../config/api';

class TripService {
  constructor() {
    this.api = axios.create({
      baseURL: API_ENDPOINTS.base,
      withCredentials: true, // Important for session handling
      timeout: 30000
    });
    
    // Request interceptor for loading states
    this.api.interceptors.request.use(
      (config) => {
        // Show loading spinner
        return config;
      },
      (error) => Promise.reject(error)
    );
    
    // Response interceptor for error handling
    this.api.interceptors.response.use(
      (response) => response.data,
      (error) => {
        if (error.response?.status === 401) {
          // Redirect to login
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  // Trip Creation
  async createTrip(tripData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.CREATE_TRIP, {
        tripName: tripData.tripName,
        startDate: tripData.startDate,
        endDate: tripData.endDate,
        arrivalTime: tripData.arrivalTime,
        baseCity: tripData.baseCity,
        multiCity: tripData.multiCity,
        categories: tripData.categories,
        pacing: tripData.pacing
      });
      return response;
    } catch (error) {
      throw new Error(`Failed to create trip: ${error.message}`);
    }
  }

  // Location Search
  async searchLocations(query, city = null, biasLocation = null, maxResults = 10) {
    try {
      const params = {
        query,
        maxResults,
        ...(city && { city }),
        ...(biasLocation && { 
          biasLat: biasLocation.lat, 
          biasLng: biasLocation.lng 
        })
      };
      
      const response = await this.api.get(API_ENDPOINTS.SEARCH_LOCATIONS, { params });
      return response.results;
    } catch (error) {
      throw new Error(`Failed to search locations: ${error.message}`);
    }
  }

  // Add Place to Trip
  async addPlaceToTrip(tripId, placeData) {
    try {
      const response = await this.api.post(API_ENDPOINTS.ADD_PLACE(tripId), {
        placeName: placeData.name,
        address: placeData.address,
        latitude: placeData.latitude,
        longitude: placeData.longitude,
        category: placeData.category,
        placeId: placeData.placeId,
        estimatedDuration: placeData.estimatedDuration || "2 hours"
      });
      return response;
    } catch (error) {
      throw new Error(`Failed to add place: ${error.message}`);
    }
  }

  // Get AI Suggestions
  async getSuggestions(tripId, day = null) {
    try {
      const params = day ? { day } : {};
      const response = await this.api.get(API_ENDPOINTS.GET_SUGGESTIONS(tripId), { params });
      return response;
    } catch (error) {
      throw new Error(`Failed to get suggestions: ${error.message}`);
    }
  }

  // Get Day Plan
  async getDayPlan(tripId, day) {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_DAY_PLAN(tripId, day));
      return response;
    } catch (error) {
      throw new Error(`Failed to get day plan: ${error.message}`);
    }
  }

  // Optimize Trip Order
  async optimizeTripOrder(tripId) {
    try {
      const response = await this.api.post(API_ENDPOINTS.OPTIMIZE_ORDER(tripId));
      return response;
    } catch (error) {
      throw new Error(`Failed to optimize trip: ${error.message}`);
    }
  }

  // Get Trip Summary
  async getTripSummary(tripId) {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_TRIP_SUMMARY(tripId));
      return response;
    } catch (error) {
      throw new Error(`Failed to get trip summary: ${error.message}`);
    }
  }

  // Get User's Trips
  async getMyTrips() {
    try {
      const response = await this.api.get(API_ENDPOINTS.GET_MY_TRIPS);
      return response.trips;
    } catch (error) {
      throw new Error(`Failed to get trips: ${error.message}`);
    }
  }
}

export default new TripService();
```

## UI Components Structure

### 1. Trip Creation Wizard
```jsx
// components/TripCreation/TripWizard.jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import tripService from '../../services/tripService';
import { CATEGORIES, PACING_OPTIONS, SRI_LANKA_CITIES } from '../../utils/constants';

const TripWizard = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [tripData, setTripData] = useState({
    tripName: '',
    startDate: '',
    endDate: '',
    arrivalTime: '09:00',
    baseCity: '',
    multiCity: false,
    categories: [],
    pacing: 'NORMAL'
  });
  const [loading, setLoading] = useState(false);

  const handleCreateTrip = async () => {
    setLoading(true);
    try {
      const response = await tripService.createTrip(tripData);
      navigate(`/trip/${response.tripId}/planning`);
    } catch (error) {
      console.error('Trip creation failed:', error);
      // Show error notification
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      {/* Progress Indicator */}
      <div className="mb-8">
        <div className="flex items-center justify-between">
          {[1, 2, 3, 4].map((step) => (
            <div key={step} className={`flex items-center ${step < 4 ? 'flex-1' : ''}`}>
              <div className={`w-8 h-8 rounded-full flex items-center justify-center ${
                currentStep >= step ? 'bg-blue-500 text-white' : 'bg-gray-300'
              }`}>
                {step}
              </div>
              {step < 4 && <div className="flex-1 h-1 bg-gray-300 mx-2" />}
            </div>
          ))}
        </div>
        <div className="flex justify-between mt-2 text-sm">
          <span>Basic Info</span>
          <span>Destinations</span>
          <span>Preferences</span>
          <span>Review</span>
        </div>
      </div>

      {/* Step 1: Basic Information */}
      {currentStep === 1 && (
        <div className="space-y-6">
          <h2 className="text-2xl font-bold">Tell us about your trip</h2>
          
          <div>
            <label className="block text-sm font-medium mb-2">Trip Name</label>
            <input
              type="text"
              value={tripData.tripName}
              onChange={(e) => setTripData({...tripData, tripName: e.target.value})}
              placeholder="e.g., Sri Lanka Adventure 2024"
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium mb-2">Start Date</label>
              <input
                type="date"
                value={tripData.startDate}
                onChange={(e) => setTripData({...tripData, startDate: e.target.value})}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-2">End Date</label>
              <input
                type="date"
                value={tripData.endDate}
                onChange={(e) => setTripData({...tripData, endDate: e.target.value})}
                className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">Arrival Time</label>
            <input
              type="time"
              value={tripData.arrivalTime}
              onChange={(e) => setTripData({...tripData, arrivalTime: e.target.value})}
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
      )}

      {/* Step 2: Destinations */}
      {currentStep === 2 && (
        <div className="space-y-6">
          <h2 className="text-2xl font-bold">Where do you want to go?</h2>
          
          <div>
            <label className="block text-sm font-medium mb-2">Starting City</label>
            <select
              value={tripData.baseCity}
              onChange={(e) => setTripData({...tripData, baseCity: e.target.value})}
              className="w-full p-3 border rounded-lg focus:ring-2 focus:ring-blue-500"
            >
              <option value="">Select your starting city</option>
              {SRI_LANKA_CITIES.map(city => (
                <option key={city.value} value={city.value}>{city.label}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={tripData.multiCity}
                onChange={(e) => setTripData({...tripData, multiCity: e.target.checked})}
                className="rounded"
              />
              <span>Multi-city trip (visit multiple cities/provinces)</span>
            </label>
          </div>

          {tripData.multiCity && (
            <div className="bg-blue-50 p-4 rounded-lg">
              <h3 className="font-medium mb-2">Multi-City Options</h3>
              <p className="text-sm text-gray-600 mb-3">
                Perfect for exploring different provinces! Popular routes:
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-2 text-sm">
                <div>• Colombo → Kandy → Nuwara Eliya</div>
                <div>• Galle → Mirissa → Yala → Ella</div>
                <div>• Trincomalee → Sigiriya → Anuradhapura</div>
                <div>• Negombo → Dambulla → Polonnaruwa</div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Step 3: Categories & Preferences */}
      {currentStep === 3 && (
        <div className="space-y-6">
          <h2 className="text-2xl font-bold">What interests you?</h2>
          
          <div>
            <label className="block text-sm font-medium mb-4">Select your interests (choose multiple)</label>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
              {CATEGORIES.map(category => (
                <label key={category.value} className="flex items-center space-x-3 p-3 border rounded-lg hover:bg-gray-50 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={tripData.categories.includes(category.value)}
                    onChange={(e) => {
                      if (e.target.checked) {
                        setTripData({...tripData, categories: [...tripData.categories, category.value]});
                      } else {
                        setTripData({...tripData, categories: tripData.categories.filter(c => c !== category.value)});
                      }
                    }}
                    className="rounded"
                  />
                  <div>
                    <div className="text-2xl">{category.icon}</div>
                    <div className="font-medium">{category.label}</div>
                    <div className="text-xs text-gray-500">{category.description}</div>
                  </div>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium mb-3">Trip Pacing</label>
            <div className="grid grid-cols-3 gap-3">
              {PACING_OPTIONS.map(option => (
                <label key={option.value} className={`p-4 border rounded-lg cursor-pointer text-center ${
                  tripData.pacing === option.value ? 'border-blue-500 bg-blue-50' : 'hover:bg-gray-50'
                }`}>
                  <input
                    type="radio"
                    name="pacing"
                    value={option.value}
                    checked={tripData.pacing === option.value}
                    onChange={(e) => setTripData({...tripData, pacing: e.target.value})}
                    className="sr-only"
                  />
                  <div className="text-2xl mb-2">{option.icon}</div>
                  <div className="font-medium">{option.label}</div>
                  <div className="text-sm text-gray-500">{option.description}</div>
                </label>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Step 4: Review */}
      {currentStep === 4 && (
        <div className="space-y-6">
          <h2 className="text-2xl font-bold">Review your trip details</h2>
          
          <div className="bg-gray-50 p-6 rounded-lg space-y-4">
            <div><strong>Trip Name:</strong> {tripData.tripName}</div>
            <div><strong>Dates:</strong> {tripData.startDate} to {tripData.endDate}</div>
            <div><strong>Starting City:</strong> {tripData.baseCity}</div>
            <div><strong>Multi-City:</strong> {tripData.multiCity ? 'Yes' : 'No'}</div>
            <div><strong>Interests:</strong> {tripData.categories.join(', ')}</div>
            <div><strong>Pacing:</strong> {tripData.pacing}</div>
          </div>
        </div>
      )}

      {/* Navigation Buttons */}
      <div className="flex justify-between mt-8">
        <button
          onClick={() => setCurrentStep(Math.max(1, currentStep - 1))}
          disabled={currentStep === 1}
          className="px-6 py-2 text-gray-600 disabled:opacity-50"
        >
          Previous
        </button>
        
        {currentStep < 4 ? (
          <button
            onClick={() => setCurrentStep(currentStep + 1)}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
          >
            Next
          </button>
        ) : (
          <button
            onClick={handleCreateTrip}
            disabled={loading}
            className="px-6 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600 disabled:opacity-50"
          >
            {loading ? 'Creating Trip...' : 'Create Trip'}
          </button>
        )}
      </div>
    </div>
  );
};

export default TripWizard;
```

### 2. Location Search Component
```jsx
// components/TripPlanning/LocationSearch.jsx
import React, { useState, useEffect, useRef } from 'react';
import tripService from '../../services/tripService';
import { debounce } from '../../utils/helpers';

const LocationSearch = ({ tripId, onPlaceAdd, currentCity }) => {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const searchRef = useRef();

  // Debounced search function
  const debouncedSearch = useRef(
    debounce(async (searchQuery) => {
      if (searchQuery.length < 2) {
        setResults([]);
        return;
      }

      setLoading(true);
      try {
        const searchResults = await tripService.searchLocations(
          searchQuery, 
          currentCity,
          null, // bias location
          10    // max results
        );
        setResults(searchResults);
        setShowResults(true);
      } catch (error) {
        console.error('Search failed:', error);
        setResults([]);
      } finally {
        setLoading(false);
      }
    }, 300)
  ).current;

  useEffect(() => {
    debouncedSearch(query);
  }, [query, debouncedSearch]);

  const handlePlaceSelect = async (place) => {
    try {
      await tripService.addPlaceToTrip(tripId, {
        name: place.name,
        address: place.address,
        latitude: place.latitude,
        longitude: place.longitude,
        category: place.categories?.[0] || 'general',
        placeId: place.placeId
      });
      
      onPlaceAdd(place);
      setQuery('');
      setResults([]);
      setShowResults(false);
    } catch (error) {
      console.error('Failed to add place:', error);
    }
  };

  return (
    <div className="relative" ref={searchRef}>
      <div className="relative">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onFocus={() => setShowResults(true)}
          placeholder={`Search for places in ${currentCity || 'Sri Lanka'}...`}
          className="w-full p-3 pl-10 border rounded-lg focus:ring-2 focus:ring-blue-500"
        />
        <div className="absolute left-3 top-3 text-gray-400">
          🔍
        </div>
        {loading && (
          <div className="absolute right-3 top-3">
            <div className="animate-spin w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full"></div>
          </div>
        )}
      </div>

      {/* Search Results */}
      {showResults && results.length > 0 && (
        <div className="absolute z-50 w-full mt-1 bg-white border rounded-lg shadow-lg max-h-96 overflow-y-auto">
          {results.map((place, index) => (
            <div
              key={index}
              onClick={() => handlePlaceSelect(place)}
              className="p-3 hover:bg-gray-50 cursor-pointer border-b last:border-b-0"
            >
              <div className="flex items-start space-x-3">
                <div className="flex-shrink-0 w-12 h-12 bg-gray-200 rounded-lg overflow-hidden">
                  {place.photoUrl ? (
                    <img src={place.photoUrl} alt={place.name} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-400">
                      📍
                    </div>
                  )}
                </div>
                <div className="flex-1">
                  <h3 className="font-medium text-gray-900">{place.name}</h3>
                  <p className="text-sm text-gray-600">{place.address}</p>
                  <div className="flex items-center mt-1 space-x-2">
                    {place.rating && (
                      <span className="text-sm text-yellow-600">
                        ⭐ {place.rating}
                      </span>
                    )}
                    {place.categories?.map(category => (
                      <span key={category} className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded">
                        {category}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* No Results */}
      {showResults && query.length >= 2 && results.length === 0 && !loading && (
        <div className="absolute z-50 w-full mt-1 bg-white border rounded-lg shadow-lg p-4 text-center text-gray-500">
          No places found for "{query}"
        </div>
      )}
    </div>
  );
};

export default LocationSearch;
```

### 3. Trip Planning Dashboard
```jsx
// components/TripPlanning/TripDashboard.jsx
import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import tripService from '../../services/tripService';
import LocationSearch from './LocationSearch';
import SuggestionPanel from './SuggestionPanel';
import TripMap from '../Maps/TripMap';

const TripDashboard = () => {
  const { tripId } = useParams();
  const [trip, setTrip] = useState(null);
  const [suggestions, setSuggestions] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    loadTripData();
  }, [tripId]);

  const loadTripData = async () => {
    try {
      const [tripData, suggestionsData] = await Promise.all([
        tripService.getTripSummary(tripId),
        tripService.getSuggestions(tripId)
      ]);
      setTrip(tripData);
      setSuggestions(suggestionsData);
    } catch (error) {
      console.error('Failed to load trip data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePlaceAdd = () => {
    loadTripData(); // Refresh trip data
  };

  const handleOptimizeTrip = async () => {
    try {
      await tripService.optimizeTripOrder(tripId);
      loadTripData(); // Refresh to show optimized order
    } catch (error) {
      console.error('Failed to optimize trip:', error);
    }
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">Loading...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      {/* Trip Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-bold text-gray-900">{trip.tripName}</h1>
        <p className="text-gray-600">
          {trip.startDate} to {trip.endDate} • {trip.baseCity}
          {trip.multiCity && ' + Multiple Cities'}
        </p>
        <div className="flex items-center mt-2 space-x-4">
          <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm">
            {trip.status}
          </span>
          <span className="text-sm text-gray-600">
            {trip.places?.length || 0} places • {trip.totalDays} days
          </span>
        </div>
      </div>

      {/* Tab Navigation */}
      <div className="border-b mb-6">
        <nav className="flex space-x-8">
          {['overview', 'planning', 'map', 'itinerary'].map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab
                  ? 'border-blue-500 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              }`}
            >
              {tab.charAt(0).toUpperCase() + tab.slice(1)}
            </button>
          ))}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Content */}
        <div className="lg:col-span-2">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold mb-4">Trip Overview</h2>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <h3 className="font-medium text-gray-900">Duration</h3>
                    <p className="text-gray-600">{trip.totalDays} days</p>
                  </div>
                  <div>
                    <h3 className="font-medium text-gray-900">Places</h3>
                    <p className="text-gray-600">{trip.places?.length || 0} locations</p>
                  </div>
                  <div>
                    <h3 className="font-medium text-gray-900">Categories</h3>
                    <p className="text-gray-600">{trip.categories?.join(', ')}</p>
                  </div>
                  <div>
                    <h3 className="font-medium text-gray-900">Pacing</h3>
                    <p className="text-gray-600">{trip.pacing}</p>
                  </div>
                </div>
              </div>

              {/* Places List */}
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex justify-between items-center mb-4">
                  <h2 className="text-xl font-semibold">Your Places</h2>
                  <button
                    onClick={handleOptimizeTrip}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                  >
                    Optimize Order
                  </button>
                </div>
                <div className="space-y-3">
                  {trip.places?.map((place, index) => (
                    <div key={index} className="flex items-center space-x-3 p-3 border rounded-lg">
                      <div className="w-8 h-8 bg-blue-500 text-white rounded-full flex items-center justify-center text-sm">
                        {index + 1}
                      </div>
                      <div className="flex-1">
                        <h3 className="font-medium">{place.name}</h3>
                        <p className="text-sm text-gray-600">{place.address}</p>
                      </div>
                      <span className="px-2 py-1 bg-gray-100 text-gray-800 text-xs rounded">
                        {place.category}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {activeTab === 'planning' && (
            <div className="space-y-6">
              <div className="bg-white rounded-lg shadow p-6">
                <h2 className="text-xl font-semibold mb-4">Add Places</h2>
                <LocationSearch
                  tripId={tripId}
                  onPlaceAdd={handlePlaceAdd}
                  currentCity={trip.baseCity}
                />
              </div>
            </div>
          )}

          {activeTab === 'map' && (
            <div className="bg-white rounded-lg shadow p-6">
              <TripMap tripId={tripId} places={trip.places} />
            </div>
          )}

          {activeTab === 'itinerary' && (
            <div className="space-y-4">
              {Array.from({ length: trip.totalDays }, (_, i) => i + 1).map(day => (
                <div key={day} className="bg-white rounded-lg shadow p-6">
                  <h3 className="text-lg font-semibold mb-3">Day {day}</h3>
                  <p className="text-gray-600">Coming soon: Detailed day planning</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <SuggestionPanel tripId={tripId} suggestions={suggestions} />
        </div>
      </div>
    </div>
  );
};

export default TripDashboard;
```

## Category Management

### Category Constants
```javascript
// utils/constants.js
export const CATEGORIES = [
  {
    value: 'culture',
    label: 'Cultural Heritage',
    icon: '🏛️',
    description: 'Temples, museums, historical sites',
    examples: ['Temple of the Tooth', 'Galle Fort', 'Anuradhapura']
  },
  {
    value: 'nature',
    label: 'Nature & Wildlife',
    icon: '🌿',
    description: 'National parks, gardens, wildlife',
    examples: ['Yala National Park', 'Horton Plains', 'Royal Botanical Gardens']
  },
  {
    value: 'adventure',
    label: 'Adventure',
    icon: '🧗',
    description: 'Hiking, water sports, climbing',
    examples: ['Adam\'s Peak', 'Ella Rock', 'Whitewater rafting']
  },
  {
    value: 'beaches',
    label: 'Beaches & Coast',
    icon: '🏖️',
    description: 'Beautiful beaches and coastal areas',
    examples: ['Mirissa', 'Unawatuna', 'Nilaveli']
  },
  {
    value: 'food',
    label: 'Food & Cuisine',
    icon: '🍛',
    description: 'Local restaurants and food experiences',
    examples: ['Rice and curry', 'Street food tours', 'Tea plantations']
  },
  {
    value: 'shopping',
    label: 'Shopping',
    icon: '🛍️',
    description: 'Markets, malls, local crafts',
    examples: ['Pettah Market', 'Laksala', 'Gem shops']
  },
  {
    value: 'wellness',
    label: 'Wellness & Spa',
    icon: '🧘',
    description: 'Ayurveda, spas, meditation',
    examples: ['Ayurvedic treatments', 'Yoga retreats', 'Meditation centers']
  },
  {
    value: 'entertainment',
    label: 'Entertainment',
    icon: '🎭',
    description: 'Shows, nightlife, festivals',
    examples: ['Cultural shows', 'Casinos', 'Night markets']
  }
];

export const PACING_OPTIONS = [
  {
    value: 'RELAXED',
    label: 'Relaxed',
    icon: '🐌',
    description: '2-3 places per day, plenty of rest time'
  },
  {
    value: 'NORMAL',
    label: 'Normal',
    icon: '🚶',
    description: '3-4 places per day, balanced schedule'
  },
  {
    value: 'ACTIVE',
    label: 'Active',
    icon: '🏃',
    description: '4-6 places per day, packed schedule'
  }
];

export const SRI_LANKA_CITIES = [
  { value: 'Colombo', label: 'Colombo - Western Province' },
  { value: 'Kandy', label: 'Kandy - Central Province' },
  { value: 'Galle', label: 'Galle - Southern Province' },
  { value: 'Trincomalee', label: 'Trincomalee - Eastern Province' },
  { value: 'Jaffna', label: 'Jaffna - Northern Province' },
  { value: 'Anuradhapura', label: 'Anuradhapura - North Central Province' },
  { value: 'Ratnapura', label: 'Ratnapura - Sabaragamuwa Province' },
  { value: 'Kurunegala', label: 'Kurunegala - North Western Province' },
  { value: 'Badulla', label: 'Badulla - Uva Province' },
  { value: 'Nuwara Eliya', label: 'Nuwara Eliya - Central Province' },
  { value: 'Negombo', label: 'Negombo - Western Province' },
  { value: 'Sigiriya', label: 'Sigiriya - Central Province' },
  { value: 'Ella', label: 'Ella - Uva Province' },
  { value: 'Mirissa', label: 'Mirissa - Southern Province' },
  { value: 'Bentota', label: 'Bentota - Southern Province' }
];
```

## State Management

### Redux Store Setup
```javascript
// store/tripSlice.js
import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import tripService from '../services/tripService';

// Async thunks
export const createTrip = createAsyncThunk(
  'trip/create',
  async (tripData, { rejectWithValue }) => {
    try {
      return await tripService.createTrip(tripData);
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const fetchTripSummary = createAsyncThunk(
  'trip/fetchSummary',
  async (tripId, { rejectWithValue }) => {
    try {
      return await tripService.getTripSummary(tripId);
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

export const addPlaceToTrip = createAsyncThunk(
  'trip/addPlace',
  async ({ tripId, placeData }, { rejectWithValue }) => {
    try {
      return await tripService.addPlaceToTrip(tripId, placeData);
    } catch (error) {
      return rejectWithValue(error.message);
    }
  }
);

const tripSlice = createSlice({
  name: 'trip',
  initialState: {
    currentTrip: null,
    myTrips: [],
    suggestions: null,
    loading: false,
    error: null
  },
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearCurrentTrip: (state) => {
      state.currentTrip = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Create Trip
      .addCase(createTrip.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createTrip.fulfilled, (state, action) => {
        state.loading = false;
        state.currentTrip = action.payload.trip;
      })
      .addCase(createTrip.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      
      // Fetch Trip Summary
      .addCase(fetchTripSummary.fulfilled, (state, action) => {
        state.currentTrip = action.payload;
      })
      
      // Add Place
      .addCase(addPlaceToTrip.fulfilled, (state, action) => {
        state.currentTrip = action.payload.trip;
      });
  }
});

export const { clearError, clearCurrentTrip } = tripSlice.actions;
export default tripSlice.reducer;
```

## Multi-City Trip Flow

### Multi-City Planning Component
```jsx
// components/MultiCity/MultiCityPlanner.jsx
import React, { useState } from 'react';

const MultiCityPlanner = ({ trip, onUpdateTrip }) => {
  const [cities, setCities] = useState(trip.cities || [trip.baseCity]);
  const [selectedCity, setSelectedCity] = useState(trip.baseCity);

  const addCity = (newCity) => {
    if (!cities.includes(newCity)) {
      setCities([...cities, newCity]);
    }
  };

  return (
    <div className="space-y-6">
      {/* City Timeline */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Your Multi-City Journey</h2>
        <div className="flex items-center space-x-4 overflow-x-auto pb-2">
          {cities.map((city, index) => (
            <div key={city} className="flex items-center">
              <div 
                onClick={() => setSelectedCity(city)}
                className={`px-4 py-2 rounded-lg cursor-pointer whitespace-nowrap ${
                  selectedCity === city ? 'bg-blue-500 text-white' : 'bg-gray-100'
                }`}
              >
                <div className="font-medium">{city}</div>
                <div className="text-sm opacity-75">
                  Day {index * 2 + 1}-{(index + 1) * 2}
                </div>
              </div>
              {index < cities.length - 1 && (
                <div className="text-gray-400 px-2">→</div>
              )}
            </div>
          ))}
          <button className="px-4 py-2 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-blue-500 hover:text-blue-500">
            + Add City
          </button>
        </div>
      </div>

      {/* City-Specific Planning */}
      <div className="bg-white rounded-lg shadow p-6">
        <h3 className="text-lg font-semibold mb-4">Planning for {selectedCity}</h3>
        
        {/* Category-based suggestions for current city */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {CATEGORIES.map(category => (
            <div key={category.value} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
              <div className="flex items-center space-x-2 mb-2">
                <span className="text-xl">{category.icon}</span>
                <span className="font-medium">{category.label}</span>
              </div>
              <p className="text-sm text-gray-600 mb-3">{category.description}</p>
              <button className="text-blue-500 text-sm hover:underline">
                Find {category.label.toLowerCase()} in {selectedCity}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default MultiCityPlanner;
```

This comprehensive frontend integration guide provides:

1. **Complete API integration** with proper error handling
2. **Modern UI components** with responsive design
3. **Category-based planning** for all Sri Lankan attractions
4. **Multi-city support** with visual journey planning
5. **State management** with Redux Toolkit
6. **Search functionality** with debouncing and real-time results
7. **Map integration** for visual trip planning
8. **Mobile-responsive design** for all screen sizes

The frontend connects seamlessly with your backend endpoints and provides an intuitive user experience for planning trips across multiple cities and provinces in Sri Lanka! 🇱🇰✨
