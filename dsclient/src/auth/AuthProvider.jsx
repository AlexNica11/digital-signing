import {createContext, useContext, useState} from "react";
import {useNavigate} from "react-router-dom";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

const AuthContext = createContext();

const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(secureLocalStorage.getItem("securityToken") || "");
    const navigate = useNavigate();
    const loginAction = async (data) => {
        try {
            let data1 = JSON.stringify(data);

            console.log(data1);

            const response  = await axios({
                method: 'post',
                url: `/api/users/login`,
                data: {
                    username: data.username,
                    password: data.password
                },
                headers: {
                    Accept: 'application/json',
                    'Content-Type': 'application/json'
                }
            });
            console.log(response);
            const res = await response.data;
            console.log(res.jwt);
            if (res.jwt) {
                setUser(data.username);
                setToken(res.jwt);
                secureLocalStorage.setItem("securityToken", res.jwt);
                secureLocalStorage.setItem("username", data.username);
                navigate("/");
                return;
            }
            throw new Error(res.message);
        } catch (err) {
            console.error(err);
        }
    };

    const logOut = () => {
        setUser(null);
        setToken("");
        secureLocalStorage.removeItem("securityToken");
        navigate("/login");
    };

    return (
        <AuthContext.Provider value={{ token, user, loginAction, logOut }}>
            {children}
        </AuthContext.Provider>
    );

};

export default AuthProvider;

export const useAuth = () => {
    return useContext(AuthContext);
};
