import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import AuthProvider from "./auth/AuthProvider.jsx";
import Login from "./auth/Login.jsx";
import PrivateRoute from "./auth/PrivateRoute.jsx";
import Dashboard from "./routes/Dashboard.jsx";
import ErrorPage from "./routes/ErrorPage.jsx";
import ProfilePage from "./routes/ProfilePage.jsx";
import SignDocumentPage from "./routes/SignDocumentPage.jsx";
import UploadKeyStorePage from "./routes/UploadKeyStorePage.jsx";
import KeyStorePage from "./routes/KeyStorePage.jsx";
import DocumentStatusPage from "./routes/DocumentStatusPage.jsx";
import Signup from "./auth/Signup.jsx";


function App() {
    return (
        <div className="App">
            <Router>
                <AuthProvider>
                    <Routes>
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<Signup />} />
                        <Route element={<PrivateRoute />} errorElement={ErrorPage}>
                            <Route path="/" element={<Dashboard />}>
                                <Route path="/profile" element={<ProfilePage/>}/>
                                <Route path="/signDocument" element={<SignDocumentPage/>}/>
                                <Route path="/documentStatus" element={<DocumentStatusPage/>}/>
                                <Route path="/uploadKeyStore" element={<UploadKeyStorePage/>}/>
                                <Route path="/keyStore/:keyStoreName" element={<KeyStorePage/>}/>
                            </Route>
                        </Route>
                        {/* Other routes */}
                    </Routes>
                </AuthProvider>
            </Router>
        </div>
    );
}

export default App;
