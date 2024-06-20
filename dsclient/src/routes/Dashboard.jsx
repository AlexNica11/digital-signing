import { useAuth } from "../auth/AuthProvider.jsx";
import {NavLink, Outlet} from "react-router-dom";
import secureLocalStorage from "react-secure-storage";

const Dashboard = () => {
    const auth = useAuth();
    return (
        <>
            <div>
                <nav>
                    <div className="nav-wrapper">
                        <button onClick={() => auth.logOut()} className="btn-large text right">
                            logout
                        </button>
                        <h5 className="blue-text darken-4 text right">{secureLocalStorage.getItem("username")}</h5>
                        <ul id="nav-mobile" className="left hide-on-med-and-down">
                            <li>
                                <NavLink
                                    to={`/signDocument`}
                                    className="btn-large text"
                                >
                                    Sign Document
                                </NavLink>
                            </li>
                            <li>
                                <NavLink
                                    to={`/documentStatus`}
                                    className="btn-large text"
                                >
                                    Document Status
                                </NavLink>
                            </li>
                            <li>
                                <NavLink
                                    to={`/profile`}
                                    className="btn-large text"
                                >
                                    Profile
                                </NavLink>
                            </li>
                        </ul>
                    </div>
                </nav>
            </div>
            <div id="detail" className="card-panel" style={{width: "50%", position: "absolute", left: "25%", top:"10%"}}>
                <Outlet/>
            </div>
        </>
    );
};

export default Dashboard;
