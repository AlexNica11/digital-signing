import { useAuth } from "../auth/AuthProvider.jsx";
import {NavLink, Outlet} from "react-router-dom";
import secureLocalStorage from "react-secure-storage";

const Dashboard = () => {
    const auth = useAuth();
    return (
        <>
            <div id="sidebar">
                <div className="container">
                    <div>
                        <h2>Welcome! {secureLocalStorage.getItem("username")}</h2>
                        <button onClick={() => auth.logOut()} className="btn-submit">
                            logout
                        </button>
                    </div>
                </div>
                <nav>
                    <ul>
                        <li>
                            <NavLink
                                to={`/signDocument`}
                                className={({isActive, isPending}) =>
                                    isActive
                                        ? "active"
                                        : isPending
                                            ? "pending"
                                            : ""
                                }
                            >
                                Sign Document
                            </NavLink>
                        </li>
                        <li>
                            <NavLink
                                to={`/documentStatus`}
                                className={({isActive, isPending}) =>
                                    isActive
                                        ? "active"
                                        : isPending
                                            ? "pending"
                                            : ""
                                }
                            >
                                Document Status
                            </NavLink>
                        </li>
                        <li>
                            <NavLink
                                to={`/profile`}
                                className={({isActive, isPending}) =>
                                    isActive
                                        ? "active"
                                        : isPending
                                            ? "pending"
                                            : ""
                                }
                            >
                                Profile
                            </NavLink>
                        </li>
                    </ul>
                </nav>
            </div>
            <div id="detail">
                <Outlet/>
            </div>
        </>
    );
};

export default Dashboard;
