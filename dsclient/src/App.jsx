import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import AuthProvider from "./auth/AuthProvider.jsx";
import Login from "./auth/Login.jsx";
import PrivateRoute from "./auth/PrivateRoute.jsx";
import Dashboard from "./auth/Dashboard.jsx";


function App() {
    return (
        <div className="App">
            <Router>
                <AuthProvider>
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route element={<PrivateRoute />}>
                            <Route path="/dashboard" element={<Dashboard />} />
                        </Route>
                        {/* Other routes */}
                    </Routes>
                </AuthProvider>
            </Router>
        </div>
    );
}

export default App;
